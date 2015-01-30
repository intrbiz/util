package com.intrbiz.queue.rabbit;

import java.io.IOException;

import com.codahale.metrics.Timer;
import com.intrbiz.queue.Producer;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.RoutingKey;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

public abstract class RabbitProducer<T, K extends RoutingKey> extends RabbitBase<T> implements Producer<T>, RoutedProducer<T, K>
{
    protected String exchange;
    
    protected K defaultKey;
    
    protected final Timer publishTimer;

    public RabbitProducer(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder, K defaultKey, Timer publishTimer)
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
    
    protected abstract String setupExchange(Channel channel) throws IOException;
    
    protected final void setup() throws IOException
    {
        this.exchange = this.setupExchange(this.channel);
    }

    protected void publish(RoutingKey key, BasicProperties props, byte[] event)
    {
        if (this.closed) throw new QueueException("This producer is closed, cannot publish");
        Timer.Context ctx = this.publishTimer.time();
        try
        {
            try
            {
                this.channel.basicPublish(this.exchange, key == null ? null : key.toString(), props, event);
            }
            catch (IOException e)
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
                new BasicProperties.Builder().contentType("").deliveryMode(2).build(), 
                this.transcoder.encodeAsBytes(event)
        );
    }

    @Override
    public void publish(T event)
    {
        if (this.defaultKey == null) throw new QueueException("No default key is given, cannot publish, did you mean to use: publish(K key, T event)?");
        this.publish(
                this.defaultKey,
                new BasicProperties.Builder().contentType("").deliveryMode(2).build(), 
                this.transcoder.encodeAsBytes(event)
        );
    }
    
    @Override
    public void publish(K key, T event, long ttl)
    {
        this.publish(
                key, 
                new BasicProperties.Builder().contentType("").deliveryMode(2).expiration(Long.toString(ttl)).build(), 
                this.transcoder.encodeAsBytes(event)
        );
    }

    @Override
    public void publish(T event, long ttl)
    {
        if (this.defaultKey == null) throw new QueueException("No default key is given, cannot publish, did you mean to use: publish(K key, T event)?");
        this.publish(
                this.defaultKey,
                new BasicProperties.Builder().contentType("").deliveryMode(2).expiration(Long.toString(ttl)).build(), 
                this.transcoder.encodeAsBytes(event)
        );
    }
}
