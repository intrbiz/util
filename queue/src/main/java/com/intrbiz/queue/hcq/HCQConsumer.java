package com.intrbiz.queue.hcq;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;

import com.codahale.metrics.Timer;
import com.intrbiz.hcq.client.HCQBatch;
import com.intrbiz.hcq.client.HCQClient;
import com.intrbiz.hcq.model.message.queue.ReceiveMessageFromQueue;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.name.RoutingKey;

public abstract class HCQConsumer<T, K extends RoutingKey> extends HCQBase<T> implements Consumer<T, K>
{
    private static final Logger logger = Logger.getLogger(HCQConsumer.class);
    
    protected DeliveryHandler<T> handler;

    protected String queue;
    
    protected Set<K> bindings = new CopyOnWriteArraySet<K>();
    
    protected final Timer consumeTimer;
    
    protected final boolean requeueOnError;
    
    public HCQConsumer(QueueBrokerPool<HCQClient> broker, QueueEventTranscoder<T> transcoder, DeliveryHandler<T> handler, Timer consumeTimer, boolean requeueOnError)
    {
        super(broker, transcoder);
        this.handler = handler;
        this.consumeTimer = consumeTimer;
        this.requeueOnError = requeueOnError;
        this.init();
    }
    
    public HCQConsumer(QueueBrokerPool<HCQClient> broker, QueueEventTranscoder<T> transcoder, DeliveryHandler<T> handler, Timer consumeTimer)
    {
        this(broker, transcoder, handler, consumeTimer, true);
    }
    
    /**
     * Create the queue we will consume from
     * @param on the client to create it with
     * @return the queue name
     */
    protected abstract String setupQueue(HCQBatch on) throws Exception;
    
    /**
     * Add a binding to our queue
     * @param on the client to bind it with
     * @param binding the binding to add
     */
    protected void addQueueBinding(HCQBatch on, String binding) throws Exception
    {
    }
    
    /**
     * Remove a binding from our queue
     * @param on the client to unbind it with
     * @param binding the binding to remove
     */
    protected void removeQueueBinding(HCQBatch on, String binding) throws Exception
    {
    }
    
    public Set<K> getBindings()
    {
        return Collections.unmodifiableSet(this.bindings);
    }
    
    @Override
    public synchronized final void addBinding(K binding)
    {
        if (binding != null)
        {
            this.bindings.add(binding);
            try
            {
                HCQBatch batch = this.client.batch();
                this.addQueueBinding(batch, binding.toString());
                batch.submit().sync();
            }
            catch (Exception e)
            {
                throw new QueueException("Failed to add binding", e);
            }
        }
    }
    
    @Override
    public synchronized final void removeBinding(K binding)
    {
        if (binding != null)
        {
            this.bindings.remove(binding);
            try
            {
                HCQBatch batch = this.client.batch();
                this.removeQueueBinding(batch, binding.toString());
                batch.submit().sync();
            }
            catch (Exception e)
            {
                throw new QueueException("Failed to remove binding", e);
            }
        }
    }
    
    protected void setupBindings(HCQBatch on) throws Exception
    {
        for (K binding : this.bindings)
        {
            if (binding != null)
            {
                this.addQueueBinding(on, binding.toString());
            }
        }
    }

    protected synchronized final void setup() throws Exception
    {
        HCQBatch batch = this.client.batch();
        this.queue = this.setupQueue(batch);
        this.setupBindings(batch);
        batch.submit().sync();
        this.setupConsumer();
    }
    
    protected void setupConsumer() throws Exception
    {
        this.client.startConsumingQueue(this.queue, this::handleDelivery).sync();
    }

    protected void handleDelivery(ReceiveMessageFromQueue message)
    {
        Timer.Context ctx = this.consumeTimer.time();
        try
        {
            try
            {
                // decode the message
                T event = this.transcoder.decodeFromString(message.getMessage().getPayload());
                // build the headers
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("queue", message.getQueueName());
                // call the delivery handler
                this.handler.handleDevliery(headers, event);
            }
            catch (Exception e)
            {
                logger.error("Error handling message delivery", e);
            }
        }
        finally
        {
            ctx.stop();
        }
    }

    @Override
    public String name()
    {
        return null;
    }

    @Override
    public DeliveryHandler<T> handler()
    {
        return this.handler;
    }
    
    @Override
    public boolean requeueOnError()
    {
        return this.requeueOnError;
    }
}
