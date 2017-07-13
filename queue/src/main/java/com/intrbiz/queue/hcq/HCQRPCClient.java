package com.intrbiz.queue.hcq;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.hcq.client.HCQBatch;
import com.intrbiz.hcq.client.HCQClient;
import com.intrbiz.hcq.model.message.queue.ReceiveMessageFromQueue;
import com.intrbiz.hcq.model.message.type.QueuedMessage;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.Queue;
import com.intrbiz.queue.name.RoutingKey;

public class HCQRPCClient<T, R, K extends RoutingKey> extends HCQBase<T> implements RPCClient<T, R, K>
{
    private static final Logger logger = Logger.getLogger(HCQRPCClient.class);
    
    private final Exchange exchange;
    
    protected final K defaultKey;
    
    private Queue replyQueue;
    
    private ConcurrentMap<String, PendingRequest> pendingRequests = new ConcurrentHashMap<String, PendingRequest>();
    
    private long timeout;
    
    private Timer timer;
    
    private final QueueEventTranscoder<R> responseTranscoder;
    
    public HCQRPCClient(QueueBrokerPool<HCQClient> broker, QueueEventTranscoder<T> transcoder, QueueEventTranscoder<R> responseTranscoder, Exchange exchange, K defaultKey, long timeout)
    {
        super(broker, transcoder);
        this.responseTranscoder = responseTranscoder;
        this.exchange = exchange;
        this.defaultKey = defaultKey;
        this.init();
        this.timeout = timeout;
        this.timer = new Timer();
    }
    
    public HCQRPCClient(QueueBrokerPool<HCQClient> broker, QueueEventTranscoder<T> transcoder, QueueEventTranscoder<R> responseTranscoder, Exchange exchange, K defaultKey)
    {
        this(broker, transcoder, responseTranscoder, exchange, defaultKey, TimeUnit.SECONDS.toMillis(10));
    }
    
    public HCQRPCClient(QueueBrokerPool<HCQClient> broker, QueueEventTranscoder<T> transcoder, QueueEventTranscoder<R> responseTranscoder, Exchange exchange)
    {
        this(broker, transcoder, responseTranscoder, exchange, null);
    }
    
    public final K defaultKey()
    {
        return this.defaultKey;
    }
    
    protected void setup() throws Exception
    {
        HCQBatch batch = this.client.batch();
        // declare the exchange
        batch.getOrCreateExchange(this.exchange.getName(), this.exchange.getType());
        // declare our temporary queue
        this.replyQueue = new Queue("rpc-client-" + UUID.randomUUID(), false);
        batch.getOrCreateQueue(this.replyQueue.getName(), ! this.replyQueue.isPersistent());
        // submit the batch
        batch.submit().sync();
        // consume responses
        this.client.startConsumingQueue(this.replyQueue.getName(), this::handleDelivery).sync();
    }
    
    protected void handleDelivery(ReceiveMessageFromQueue message)
    {
        QueuedMessage theMessage = message.getMessage();
        String correlationId = theMessage.getCorrelationId();
        if (correlationId != null)
        {
            PendingRequest request = pendingRequests.remove(correlationId);
            if (request != null)
            {
                try 
                {
                    request.complete(responseTranscoder.decodeFromString(theMessage.getPayload()));
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
    public Future<R> publish(long timeout, T event)
    {
        return this.publish(this.defaultKey, timeout, event, null, null);
    }

    @Override
    public Future<R> publish(long timeout, T event, Consumer<R> onSuccess)
    {
        return this.publish(this.defaultKey, timeout, event, onSuccess, null);
    }

    @Override
    public Future<R> publish(long timeout, T event, Consumer<R> onSuccess, Consumer<Exception> onError)
    {
        return this.publish(this.defaultKey, timeout, event, onSuccess, onError);
    }

    @Override
    public Future<R> publish(K key, long timeout, T event)
    {
        return this.publish(key, this.timeout, event, null, null);
    }
    
    @Override
    public Future<R> publish(T event, Consumer<R> onSuccess, Consumer<Exception> onError)
    {
        return this.publish(this.defaultKey, this.timeout, event, onSuccess, onError);
    }

    @Override
    public Future<R> publish(T event, Consumer<R> onSuccess)
    {
        return this.publish(this.defaultKey, this.timeout, event, onSuccess, null);
    }

    @Override
    public Future<R> publish(K key, T event, Consumer<R> onSuccess, Consumer<Exception> onError)
    {
        return this.publish(key, this.timeout, event, onSuccess, onError);
    }

    @Override
    public Future<R> publish(K key, T event, Consumer<R> onSuccess)
    {
        return this.publish(key, this.timeout, event, onSuccess, null);
    }

    @Override
    public Future<R> publish(K key, long timeout, T event, Consumer<R> onSuccess, Consumer<Exception> onError)
    {
        try
        {
            PendingRequest future = new PendingRequest(event, onSuccess, onError);
            // store the pending request
            this.pendingRequests.put(future.getId(), future);
            // schedule the timeout
            this.timer.schedule(future, timeout);
            // publish the request
            String contentType = this.transcoder.getContentType(event);
            String payload = this.transcoder.encodeAsString(event);
            this.client.publishMessageToExchange(
                    this.exchange.getName(), 
                    key == null ? "" : key.toString(), 
                    new QueuedMessage().randomMessageId().correlationId(future.getId()).replyTo(this.replyQueue.getName()).payload(contentType, payload)
            ).sync();
            return future;
        }
        catch (Exception e)
        {
            throw new QueueException("Failed to publish request", e);
        }
    }

    @Override
    public Future<R> publish(K key, long timeout, T event, Consumer<R> onSuccess)
    {
        return this.publish(key, timeout, event, onSuccess, null);
    }

    protected class PendingRequest extends TimerTask implements Future<R>
    {
        private final String id;
        
        private final T request;
        
        private final Consumer<R> onSuccess;
        
        private final Consumer<Exception> onError;
        
        private R response;
        
        private volatile boolean timeout;
        
        private volatile boolean done;
        
        public PendingRequest(T request, Consumer<R> onSuccess, Consumer<Exception> onError)
        {
            this.id = (Long.toHexString(System.currentTimeMillis()) + "-" + UUID.randomUUID()).toUpperCase();
            this.request = request;
            this.onSuccess = onSuccess;
            this.onError = onError;
        }
        
        public PendingRequest(T request, Consumer<R> onSuccess)
        {
            this(request, onSuccess, null);
        }
        
        public PendingRequest(T request)
        {
            this(request, null, null);
        }
        
        @Override
        public void run()
        {
            HCQRPCClient.this.pendingRequests.remove(this.getId());
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
            // invoke the callback
            if (this.onSuccess != null)
            {
                try
                {
                    this.onSuccess.accept(response);
                }
                catch (Exception e)
                {
                }
            }
            // wake up anything waiting for this to complete
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
            // invoke the callback
            if (this.onError != null)
            {
                try
                {
                    this.onError.accept(new QueueException("Request timedout"));
                }
                catch (Exception e)
                {
                }
            }
            // wake up anything waiting for this to complete
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
