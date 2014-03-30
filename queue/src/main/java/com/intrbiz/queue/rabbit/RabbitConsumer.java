package com.intrbiz.queue.rabbit;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.Queue;
import com.intrbiz.queue.name.RoutingKey;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RabbitConsumer<T, K extends RoutingKey> extends RabbitBase<T> implements Consumer<T, K>
{
    private Logger logger = Logger.getLogger(RabbitConsumer.class);

    protected final DeliveryHandler<T> handler;

    protected final Exchange exchange;

    protected final Queue queueSpec;

    protected final Set<K> bindings = new HashSet<K>();

    protected Queue queue;

    protected String consumerName;

    @SafeVarargs
    public RabbitConsumer(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder, DeliveryHandler<T> handler, Queue queue, Exchange exchange, K... bindings)
    {
        super(broker, transcoder);
        this.handler = handler;
        this.queueSpec = queue;
        this.exchange = exchange;
        if (bindings != null)
        {
            for (K binding : bindings)
            {
                this.bindings.add(binding);
            }
        }
        this.init();
    }

    @SafeVarargs
    public RabbitConsumer(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder, DeliveryHandler<T> handler, Exchange exchange, K... bindings)
    {
        this(broker, transcoder, handler, null, exchange, bindings);
    }

    protected void setup() throws IOException
    {
        // declare the queue
        if (this.queueSpec == null)
        {
            // declare a temporary queue
            this.queue = new Queue(this.channel.queueDeclare().getQueue(), false);
        }
        else
        {
            // use the name given
            this.channel.queueDeclare(this.queueSpec.getName(), this.queueSpec.isPersistent(), false, !this.queueSpec.isPersistent(), null);
            this.queue = this.queueSpec;
        }
        // bind the queue
        // declare the exchange
        this.channel.exchangeDeclare(this.exchange.getName(), this.exchange.getType(), this.exchange.isPersistent());
        // bind our queue to the exchange with the given routing keys
        if (this.bindings.size() > 0)
        {
            for (K binding : this.bindings)
            {
                this.channel.queueBind(this.queue.getName(), this.exchange.getName(), binding.toString());
            }
        }
        else
        {
            this.channel.queueBind(this.queue.getName(), this.exchange.getName(), this.queue.toKey().toString());
        }
        // consume
        try
        {
            this.consumerName = this.channel.basicConsume(this.queue.getName(), false, new DefaultConsumer(this.channel)
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
        catch (IOException e)
        {
            throw new QueueException("Cannot declare consumer", e);
        }
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
    public Queue queue()
    {
        return this.queue;
    }

    @Override
    public Set<K> bindings()
    {
        return Collections.unmodifiableSet(this.bindings);
    }

    @Override
    public void addBinding(K key)
    {
        this.bindings.add(key);
        // bind
        try
        {
            if (this.channel != null && this.channel.isOpen())
            {
                this.channel.queueBind(this.queue.getName(), this.exchange.getName(), key.toString());
            }
        }
        catch (IOException e)
        {
            logger.error("Failed to immediately update queue bindings", e);
        }
    }

    @Override
    public DeliveryHandler<T> handler()
    {
        return this.handler;
    }
}
