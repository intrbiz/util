package com.intrbiz.queue.rabbit;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public abstract class RabbitConsumer<T> extends RabbitBase<T> implements Consumer<T>
{
    private Logger logger = Logger.getLogger(RabbitConsumer.class);

    protected DeliveryHandler<T> handler;

    protected String queue;

    protected String consumerName;
    
    protected Set<String> bindings = new HashSet<String>();
    
    public RabbitConsumer(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder, DeliveryHandler<T> handler)
    {
        super(broker, transcoder);
        this.handler = handler;
        this.init();
    }
    
    protected abstract String setupQueue(Channel on) throws IOException;
    
    protected void addQueueBinding(Channel on, String binding) throws IOException
    {
    }
    
    @Override
    public void addBinding(String binding)
    {
        this.bindings.add(binding);
        try
        {
            this.addQueueBinding(this.channel, binding);
        }
        catch (Exception e)
        {
            throw new QueueException("Failed to add binding", e);
        }
    }
    
    protected void setupBindings() throws IOException
    {
        for (String binding : this.bindings)
        {
            this.addQueueBinding(this.channel, binding);
        }
    }

    protected final void setup() throws IOException
    {
        this.queue = this.setupQueue(this.channel);
        this.setupBindings();
        this.setupConsumer();
    }
    
    protected void setupConsumer() throws IOException
    {
        this.consumerName = this.channel.basicConsume(this.queue, false, new DefaultConsumer(this.channel)
        {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException
            {
                try
                {
                    RabbitConsumer.this.handleDelivery(consumerTag, envelope, properties, body);
                }
                catch (Exception e)
                {
                    logger.error("Unhandled error handling delivery", e);
                }
            }
        });
    }

    protected void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException
    {
        // decode the event
        T event = this.transcoder.decodeFromBytes(body);
        // handle the event
        this.handler.handleDevliery(event);
        // ack the event
        this.channel.basicAck(envelope.getDeliveryTag(), false);
    }

    @Override
    public String name()
    {
        return this.consumerName;
    }

    @Override
    public DeliveryHandler<T> handler()
    {
        return this.handler;
    }
}
