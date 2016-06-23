package com.intrbiz.queue.rabbit;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;

import com.codahale.metrics.Timer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.MultiConsumer;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.name.RoutingKey;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public abstract class RabbitConsumer<T, K extends RoutingKey> extends RabbitBase<T> implements MultiConsumer<T, K>
{
    private Logger logger = Logger.getLogger(RabbitConsumer.class);

    protected DeliveryHandler<T> handler;

    protected String queue;

    protected String[] consumerNames;
    
    protected Set<K> bindings = new CopyOnWriteArraySet<K>();
    
    protected final Timer consumeTimer;
    
    protected final int threads;
    
    protected final boolean requeueOnError;
    
    public RabbitConsumer(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder, DeliveryHandler<T> handler, Timer consumeTimer, int threads, boolean requeueOnError)
    {
        super(broker, transcoder);
        this.handler = handler;
        this.consumeTimer = consumeTimer;
        this.threads = threads;
        this.requeueOnError = requeueOnError;
        this.init();
    }
    
    public RabbitConsumer(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder, DeliveryHandler<T> handler, Timer consumeTimer, int threads)
    {
        this(broker, transcoder, handler, consumeTimer, threads, true);
    }
    
    public RabbitConsumer(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder, DeliveryHandler<T> handler, Timer consumeTimer)
    {
        this(broker, transcoder, handler, consumeTimer, 1, true);
    }
    
    protected abstract String setupQueue(Channel on) throws IOException;
    
    protected void addQueueBinding(Channel on, String binding) throws IOException
    {
    }
    
    protected void removeQueueBinding(Channel on, String binding) throws IOException
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
                this.addQueueBinding(this.channel, binding.toString());
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
                this.removeQueueBinding(this.channel, binding.toString());
            }
            catch (Exception e)
            {
                throw new QueueException("Failed to remove binding", e);
            }
        }
    }
    
    protected void setupBindings() throws IOException
    {
        for (K binding : this.bindings)
        {
            if (binding != null)
            {
                this.addQueueBinding(this.channel, binding.toString());
            }
        }
    }

    protected synchronized final void setup() throws IOException
    {
        this.queue = this.setupQueue(this.channel);
        this.setupBindings();
        this.setupConsumer();
    }
    
    protected void setupConsumer() throws IOException
    {
        this.consumerNames = new String[this.threads];
        for (int i = 0; i < this.threads; i++)
        {
            this.consumerNames[i] = this.channel.basicConsume(this.queue, false, new DefaultConsumer(this.channel)
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
    }

    protected void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException
    {
        Timer.Context ctx = this.consumeTimer.time();
        try
        {
            try
            {
                // decode the event
                T event = this.transcoder.decodeFromBytes(body);
                // handle the event
                this.handler.handleDevliery(properties.getHeaders(), event);
                // ack the event
                this.channel.basicAck(envelope.getDeliveryTag(), false);
            }
            catch (Exception e)
            {
                // send a nack for this delivery
                logger.error("Error handling message delivery, rejecting" + (this.requeueOnError ? " for requeue" : ""), e);
                this.channel.basicNack(envelope.getDeliveryTag(), false, this.requeueOnError);
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
        return this.consumerNames[0];
    }
    
    @Override
    public String[] names()
    {
        return this.consumerNames;
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
