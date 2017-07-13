package com.intrbiz.queue.hcq;

import com.codahale.metrics.Timer;
import com.intrbiz.hcq.client.HCQBatch;
import com.intrbiz.hcq.client.HCQClient;
import com.intrbiz.hcq.model.message.type.QueuedMessage;
import com.intrbiz.queue.Producer;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.RoutingKey;

public abstract class HCQProducer<T, K extends RoutingKey> extends HCQBase<T> implements Producer<T>, RoutedProducer<T, K>
{
    protected String exchange;
    
    protected K defaultKey;
    
    protected final Timer publishTimer;

    public HCQProducer(QueueBrokerPool<HCQClient> broker, QueueEventTranscoder<T> transcoder, K defaultKey, Timer publishTimer)
    {
        super(broker, transcoder);
        this.defaultKey = defaultKey;
        this.publishTimer = publishTimer;
        this.init();
    }
    
    public final RoutingKey defaultKey()
    {
        return this.defaultKey;
    }
    
    protected abstract String setupExchange(HCQBatch client) throws Exception;
    
    protected final void setup() throws Exception
    {
        HCQBatch batch = this.client.batch();
        this.exchange = this.setupExchange(batch);
        batch.submit().sync();
    }

    @Override
    public void publish(K key, T event, long ttlSeconds)
    {
        this.checkUpAndRunning("This producer is closed, cannot publish");
        Timer.Context ctx = this.publishTimer.time();
        try
        {
            try
            {
                String contentType = this.transcoder.getContentType(event);
                String payload = this.transcoder.encodeAsString(event);
                this.client.publishMessageToExchange(
                        this.exchange, key == null ? null : key.toString(), 
                        new QueuedMessage().randomMessageId().ttl((int) ttlSeconds).payload(contentType, payload)
                ).sync();
            }
            catch (Exception e)
            {
                throw new QueueException("Cannot publish event", e);
            }
        }
        finally
        {
            ctx.stop();
        }
    }

    @Override
    public void publish(K key, T event)
    {
        this.publish(
                key, 
                event,
                0L
        );
    }

    @Override
    public void publish(T event)
    {
        if (this.defaultKey == null) throw new QueueException("No default key is given, cannot publish, did you mean to use: publish(K key, T event)?");
        this.publish(
                this.defaultKey,
                event,
                0L
        );
    }

    @Override
    public void publish(T event, long ttl)
    {
        if (this.defaultKey == null) throw new QueueException("No default key is given, cannot publish, did you mean to use: publish(K key, T event)?");
        this.publish(
                this.defaultKey, 
                event,
                ttl
        );
    }
}
