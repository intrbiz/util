package com.intrbiz.queue.rabbit;

import java.io.IOException;
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

public class RabbitRPCClient<T, R, K extends RoutingKey> extends RabbitBase<T> implements RPCClient<T, R,K>
{
    private Logger logger = Logger.getLogger(RabbitRPCClient.class);
    
    private final Exchange exchange;
    
    protected final K defaultKey;
    
    private Queue replyQueue;
    
    private ConcurrentMap<String, PendingRequest> pendingRequests = new ConcurrentHashMap<String, PendingRequest>();
    
    private long timeout;
    
    private Timer timer;
    
    private final QueueEventTranscoder<R> responseTranscoder;
    
    public RabbitRPCClient(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder, QueueEventTranscoder<R> responseTranscoder, Exchange exchange, K defaultKey, long timeout)
    {
        super(broker, transcoder);
        this.responseTranscoder = responseTranscoder;
        this.exchange = exchange;
        this.defaultKey = defaultKey;
        this.init();
        this.timeout = timeout;
        this.timer = new Timer();
    }
    
    public RabbitRPCClient(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder, QueueEventTranscoder<R> responseTranscoder, Exchange exchange, K defaultKey)
    {
        this(broker, transcoder, responseTranscoder, exchange, defaultKey, TimeUnit.SECONDS.toMillis(10));
    }
    
    public RabbitRPCClient(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder, QueueEventTranscoder<R> responseTranscoder, Exchange exchange)
    {
        this(broker, transcoder, responseTranscoder, exchange, null);
    }
    
    public final K defaultKey()
    {
        return this.defaultKey;
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
                    PendingRequest request = pendingRequests.remove(correlationId);
                    if (request != null)
                    {
                        try 
                        {
                            request.complete(responseTranscoder.decodeFromBytes(body));
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
    public Future<R> publish(T event)
    {
        return this.publish(this.defaultKey, event);
    }

    @Override
    public Future<R> publish(K key, T event)
    {
        return this.publish(key, this.timeout, event);
    }
    
    @Override
    public Future<R> publish(K key, long timeout, T event)
    {
        try
        {
            PendingRequest future = new PendingRequest(event);
            // store the pending request
            this.pendingRequests.put(future.getId(), future);
            // schedule the timeout
            this.timer.schedule(future, timeout);
            // publish the request
            this.channel.basicPublish(
                    this.exchange.getName(), 
                    key == null ? "" : key.toString(), 
                    new BasicProperties("application/json", null, null, 1, null, future.getId(), this.replyQueue.getName(), null, null, null, null, null, null, null), 
                    this.transcoder.encodeAsBytes(event)
            );
            return future;
        }
        catch (IOException e)
        {
            throw new QueueException("Failed to publish request", e);
        }
    }
    
    protected class PendingRequest extends TimerTask implements Future<R>
    {
        private final String id;
        
        private final T request;
        
        private R response;
        
        private volatile boolean timeout;
        
        private volatile boolean done;
        
        public PendingRequest(T request)
        {
            this.id = (Long.toHexString(System.currentTimeMillis()) + "-" + UUID.randomUUID()).toUpperCase();
            this.request = request;
        }
        
        @Override
        public void run()
        {
            RabbitRPCClient.this.pendingRequests.remove(this.getId());
            this.timeout();
        }
        
        public String getId()
        {
            return this.id;
        }
        
        public T getRequest()
        {
            return this.request;
        }
        
        public R getResponse()
        {
            return this.response;
        }
        
        public void complete(R response)
        {
            this.response = response;
            this.done = true;
            this.cancel();
            synchronized (this)
            {
                this.notifyAll();
            }
        }
        
        public void timeout()
        {
            this.timeout = true;
            this.response = null;
            this.done = true;
            synchronized (this)
            {
                this.notifyAll();
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning)
        {
            throw new RuntimeException("Cancelling of requests is not supported");
        }

        @Override
        public boolean isCancelled()
        {
            return false;
        }

        @Override
        public boolean isDone()
        {
            return this.done;
        }

        @Override
        public R get() throws InterruptedException, ExecutionException
        {
            if (! this.done)
            {
                synchronized (this)
                {
                    this.wait();
                }
            }
            if (this.timeout) throw new QueueException("Request timeout");
            return this.response;
        }

        @Override
        public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
        {
            if (! this.done)
            {
                synchronized (this)
                {
                    this.wait(unit.toMillis(timeout));
                }
            }
            if (this.timeout) throw new QueueException("Request timeout");
            return this.response;
        }
    }
}
