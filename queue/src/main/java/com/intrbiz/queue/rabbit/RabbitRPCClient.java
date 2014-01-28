package com.intrbiz.queue.rabbit;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.Queue;
import com.intrbiz.queue.name.RoutingKey;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RabbitRPCClient<T, K extends RoutingKey> extends RabbitBase<T> implements RPCClient<T,K>
{
    private Logger logger = Logger.getLogger(RabbitRPCClient.class);
    
    private final Exchange exchange;
    
    private Queue replyQueue;
    
    private ConcurrentMap<String, PendingRequest> pendingRequests = new ConcurrentHashMap<String, PendingRequest>();
    
    private long timeout = TimeUnit.MINUTES.toMillis(10);
    
    private Timer vacuumCleaner;
    
    public RabbitRPCClient(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder, Exchange exchange)
    {
        super(broker, transcoder);
        this.exchange = exchange;
        this.init();
        this.vacuumCleaner = new Timer(true);
        this.vacuumCleaner.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                vacuum();
            }
            
        }, this.timeout, this.timeout);
    }
    
    protected void vacuum()
    {
        long now = System.currentTimeMillis();
        for (Entry<String, PendingRequest> entry : this.pendingRequests.entrySet())
        {
            if ((now - entry.getValue().getSentAt()) > this.timeout) 
                this.pendingRequests.remove(entry.getKey());
        }
    }
    
    protected void setup() throws IOException
    {
        // declare the exchange
        this.channel.exchangeDeclare(this.exchange.getName(), this.exchange.getType(), this.exchange.isPersistent());
        // declare our temporary queue
        this.replyQueue = new Queue(this.channel.queueDeclare().getQueue(), false);
        // consume responses
        this.channel.basicConsume(this.replyQueue.getName(), true, new DefaultConsumer(this.channel)
        {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException
            {
                String correlationId = properties.getCorrelationId();
                if (correlationId != null)
                {
                    PendingRequest request = pendingRequests.get(correlationId);
                    if (request != null)
                    {
                        pendingRequests.remove(correlationId);
                        try 
                        {
                            request.complete(transcoder.decodeFromBytes(body));
                        }
                        catch (Exception e)
                        {
                            logger.error("Unhandled error completing RPC request", e);
                        }
                    }
                    else
                    {
                        logger.warn("Cannot correlate request with id: " + correlationId + ", it is possible the request timedout.");
                    }
                }
                else
                {
                    logger.warn("Ignoring message with no correlation id");
                }
            }
        });
    }
    
    @Override
    public Exchange exchange()
    {
        return this.exchange;
    }
    
    @Override
    public Queue replyQueue()
    {
        return this.replyQueue;
    }
    
    @Override
    public Future<T> publish(T event)
    {
        return this.publish(null, event);
    }
    
    @Override
    public Future<T> publish(K key, T event)
    {
        try
        {
            String correlationId = (Long.toHexString(System.currentTimeMillis()) + "-" + UUID.randomUUID()).toUpperCase();
            PendingRequest future = new PendingRequest(correlationId, event, System.currentTimeMillis());
            // store the pending request
            this.pendingRequests.put(correlationId, future);
            // publish the request
            this.channel.basicPublish(
                    this.exchange.getName(), 
                    key == null ? null : key.toString(), 
                    new BasicProperties("application/json", null, null, 1, null, correlationId, this.replyQueue.getName(), null, null, null, null, null, null, null), 
                    this.transcoder.encodeAsBytes(event)
            );
            return future;
        }
        catch (IOException e)
        {
            throw new QueueException("Failed to publish request", e);
        }
    }
    
    protected class PendingRequest implements Future<T>
    {
        private String id;
        
        private final T request;
        
        private final long sentAt;
        
        private T response;
        
        private volatile boolean done;
        
        private volatile boolean cancelled;
        
        public PendingRequest(String id, T request, long sentAt)
        {
            this.request = request;
            this.sentAt = sentAt;
        }
        
        public String getId()
        {
            return this.id;
        }
        
        public T getRequest()
        {
            return this.request;
        }
        
        public long getSentAt()
        {
            return this.sentAt;
        }
        
        public T getResponse()
        {
            return this.response;
        }
        
        public void complete(T response)
        {
            this.response = response;
            this.done = true;
            synchronized (this)
            {
                this.notifyAll();
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning)
        {
            this.cancelled = true;
            RabbitRPCClient.this.pendingRequests.remove(this.id);
            synchronized (this)
            {
                this.notifyAll();
            }
            return true;
        }

        @Override
        public boolean isCancelled()
        {
            return this.cancelled;
        }

        @Override
        public boolean isDone()
        {
            return this.done;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException
        {
            synchronized (this)
            {
                this.wait();
            }
            return this.response;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
        {
            synchronized (this)
            {
                this.wait(unit.toMillis(timeout));
            }
            return this.response;
        }
    }
}
