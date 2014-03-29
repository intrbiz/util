package com.intrbiz.queue.rabbit;

import java.io.IOException;

import com.intrbiz.queue.Producer;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.RoutingKey;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

public class RabbitProducer<T, K extends RoutingKey> extends RabbitBase<T> implements Producer<T>, RoutedProducer<T, K>
{
    protected final Exchange exchange;
    
    protected final K defaultKey;

    public RabbitProducer(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder, Exchange exchange, K defaultKey)
    {
        super(broker, transcoder);
        this.exchange = exchange;
        this.defaultKey = defaultKey;
        this.init();
    }
    
    public RabbitProducer(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder, Exchange exchange)
    {
        this(broker, transcoder, exchange, null);
    }

    protected void setup() throws IOException
    {
        this.channel.exchangeDeclare(this.exchange.getName(), this.exchange.getType(), this.exchange.isPersistent());
    }

    @Override
    public final Exchange exchange()
    {
        return this.exchange;
    }
    
    public final K defaultKey()
    {
        return this.defaultKey;
    }

    protected void publish(K key, BasicProperties props, byte[] event)
    {
        if (this.closed) throw new QueueException("This producer is closed, cannot publish");
        try
        {
            this.channel.basicPublish(this.exchange.getName(), key == null ? null : key.toString(), props, event);
        }
        catch (IOException e)
        {
            throw new QueueException("Cannot publish event", e);
        }
    }

    @Override
    public void publish(K key, T event)
    {
        this.publish(
                key, 
                new BasicProperties("application/json", null, null, 2, /* Persistent */ null, null, null, null, null, null, null, null, null, null), 
                this.transcoder.encodeAsBytes(event)
        );
    }

    @Override
    public void publish(T event)
    {
        if (this.defaultKey == null) throw new QueueException("No default key is given, cannot publish, did you mean to use: publish(K key, T event)?");
        this.publish(
                this.defaultKey,
                new BasicProperties("application/json", null, null, 2, /* Persistent */ null, null, null, null, null, null, null, null, null, null), 
                this.transcoder.encodeAsBytes(event)
        );
    }
}
