package com.intrbiz.queue.rabbit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitPool implements QueueBrokerPool<Channel>
{
    public static final String TYPE = "rabbitmq";
    
    private ConnectionFactory factory;

    private volatile Connection currentConnection;
    
    private ExecutorService executor;

    public RabbitPool(String uri, String username, String password)
    {
        try
        {
            this.executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE, 6L, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(), new ThreadFactory()
            {
                private AtomicInteger count = new AtomicInteger();
                @Override
                public Thread newThread(Runnable r)
                {
                    return new Thread(r, "Rabbit-Pool-Worker-" + count.incrementAndGet());
                }
            });
            //
            this.factory = new ConnectionFactory();
            this.factory.setConnectionTimeout(5000);
            this.factory.setUri(uri);
            this.factory.setSharedExecutor(this.executor);
            this.factory.setRequestedHeartbeat(5000);
            this.factory.setAutomaticRecoveryEnabled(true);
            this.factory.setTopologyRecoveryEnabled(true);
            this.factory.setNetworkRecoveryInterval(5000);
            if (username != null) this.factory.setUsername(username);
            if (password != null) this.factory.setPassword(password);
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
    
    public RabbitPool(String uri)
    {
        this(uri, null, null);
    }

    @Override
    public Channel connect()
    {
        synchronized (this)
        {
            // open and cache our connection
            if (this.currentConnection == null )
            {
                try
                {
                    this.currentConnection = this.factory.newConnection(this.executor);
                    this.currentConnection.addShutdownListener(new ShutdownListener()
                    {
                        @Override
                        public void shutdownCompleted(ShutdownSignalException cause)
                        {
                            synchronized (RabbitPool.this)
                            {
                                RabbitPool.this.currentConnection = null;
                            }
                        }
                    });
                }
                catch (IOException e)
                {
                    throw new QueueException("Cannot connect to queue broker", e);
                }
                catch (TimeoutException e)
                {
                    throw new QueueException("Cannot connect to queue broker", e);
                }
            }
            try
            {
                return this.currentConnection.createChannel();
            }
            catch (IOException e)
            {
                throw new QueueException("Failed to open channel", e);
            }
        }
    }

    @Override
    public void close()
    {
        synchronized (this)
        {
            if (this.currentConnection != null)
            {
                try
                {
                    this.currentConnection.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }
    
    @Override
    public String type()
    {
        return TYPE;
    }
}
