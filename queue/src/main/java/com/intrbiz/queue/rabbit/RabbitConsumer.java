package com.intrbiz.queue.rabbit;

import java.io.IOException;

import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueException;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public abstract class RabbitConsumer<T> implements Consumer<T>
{
    protected final QueueBrokerPool<Connection> broker;

    protected final DeliveryHandler<T> handler;

    protected String queueName;

    protected String consumerName;

    protected volatile boolean closed;

    protected Connection connection;

    protected Channel channel;

    public RabbitConsumer(QueueBrokerPool<Connection> broker, DeliveryHandler<T> handler)
    {
        super();
        this.broker = broker;
        this.handler = handler;
        this.init();
    }

    protected void init()
    {
        if (this.closed) return;
        try
        {
            // initialise the connection and channel
            this.connection = broker.connect();
            this.channel = this.connection.createChannel();
            // we need to reinit should the connection fail
            this.connection.addShutdownListener(new Reinit());
            // declare the queue
            this.declareQueue();
            // bind the queue
            this.bindQueue();
            // consume
            this.declareConsumer();
        }
        catch (IOException e)
        {
            throw new QueueException("Cannot initialise connection", e);
        }
    }

    protected void declareQueue() throws IOException
    {
        this.queueName = this.channel.queueDeclare().getQueue();
    }

    protected void declareConsumer()
    {
        try
        {
            this.channel.basicConsume(queueName, true, new DefaultConsumer(this.channel)
            {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException
                {
                    RabbitConsumer.this.handleDelivery(consumerTag, envelope, properties, body);
                }
            });
        }
        catch (IOException e)
        {
            throw new QueueException("Cannot declare consumer", e);
        }
    }

    protected abstract void bindQueue() throws IOException;

    protected abstract void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException;

    @Override
    public String name()
    {
        return this.consumerName;
    }

    @Override
    public String queueName()
    {
        return this.queueName;
    }

    @Override
    public DeliveryHandler<T> handler()
    {
        return this.handler;
    }

    @Override
    public void close()
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
                    RabbitConsumer.this.init();
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
