package com.intrbiz.queue.rabbit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueException;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitPool implements QueueBrokerPool<Connection>
{
    private ConnectionFactory factory;

    public RabbitPool(String uri)
    {
        try
        {
            this.factory = new ConnectionFactory();
            this.factory.setUri(uri);
        }
        catch (KeyManagementException e)
        {
            throw new QueueException("Cannot init connection factory", e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new QueueException("Cannot init connection factory", e);
        }
        catch (URISyntaxException e)
        {
            throw new QueueException("Cannot init connection factory", e);
        }
    }

    @Override
    public Connection connect()
    {
        try
        {
            return this.factory.newConnection();
        }
        catch (IOException e)
        {
            throw new QueueException("Cannot connect to queue broker", e);
        }
    }

    @Override
    public void close()
    {
    }
}
