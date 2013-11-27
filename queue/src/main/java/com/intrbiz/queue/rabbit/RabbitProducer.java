package com.intrbiz.queue.rabbit;

import java.io.IOException;

import com.intrbiz.queue.Producer;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.RoutedProducer;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public abstract class RabbitProducer<T> implements Producer<T>, RoutedProducer<T>
{
    protected final QueueBrokerPool<Connection> broker;

    protected final String exchange;

    protected final String exchangeType;

    protected volatile boolean closed = false;

    protected Connection connection;

    protected Channel channel;

    public RabbitProducer(QueueBrokerPool<Connection> broker, String exchange, String exchangeType)
    {
        super();
        this.broker = broker;
        this.exchange = exchange;
        this.exchangeType = exchangeType;
        this.init();
    }

    public RabbitProducer(QueueBrokerPool<Connection> broker, String exchange)
    {
        this(broker, exchange, "topic");
    }

    protected void init()
    {
        if (this.closed) return;
        try
        {
            this.connection = broker.connect();
            this.channel = this.connection.createChannel();
            this.connection.addShutdownListener(new Reinit());
            this.decareExchange();
        }
        catch (IOException e)
        {
            throw new QueueException("Cannot initialise connection", e);
        }
    }

    protected void decareExchange() throws IOException
    {
        this.channel.exchangeDeclare(this.exchange, this.exchangeType, true);
    }

    @Override
    public final String exchange()
    {
        return this.exchange;
    }

    public final String exchangeType()
    {
        return this.exchangeType;
    }

    protected void publish(String key, BasicProperties props, byte[] event)
    {
        if (this.closed) throw new QueueException("This producer is closed, cannot publish");
        try
        {
            this.channel.basicPublish(this.exchange, key, props, event);
        }
        catch (IOException e)
        {
            throw new QueueException("Cannot publish event", e);
        }
    }

    @Override
    public final void close()
    {
        if (!this.closed)
        {
            this.closed = true;
            try
            {
                channel.close();
            }
            catch (Exception e)
            {
            }
            finally
            {
                try
                {
                    connection.close();
                }
                catch (Exception e)
                {
                }
            }
            this.channel = null;
            this.connection = null;
        }
    }

    private class Reinit implements ShutdownListener
    {
        @Override
        public void shutdownCompleted(ShutdownSignalException cause)
        {
            while (true)
            {
                try
                {
                    Thread.sleep(5000);
                    RabbitProducer.this.init();
                    break;
                }
                catch (QueueException e)
                {
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    }
}
