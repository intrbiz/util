package com.intrbiz.queue.rabbit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public abstract class RabbitBase<T> implements AutoCloseable
{
    protected QueueBrokerPool<Channel> broker;
    
    protected QueueEventTranscoder<T> transcoder;
    
    protected volatile boolean closed;

    protected Channel channel;
    
    public RabbitBase(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder)
    {
        super();
        this.broker = broker;
        this.transcoder = transcoder;
    }
    
    /**
     * Invoke this during the constructor!
     */
    protected void init()
    {
        if (this.closed) return;
        try
        {
            // initialise the connection and channel
            this.channel = broker.connect();
            // we need to reinit should the connection fail
            this.channel.addShutdownListener(new Reinit());
            // setup this thing
            this.setup();
        }
        catch (IOException e)
        {
            throw new QueueException("Cannot initialise connection", e);
        }
    }
    
    protected abstract void setup() throws IOException;
    
    //
    
    protected Map<String, Object> args(String name, Object value)
    {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put(name, value);
        return args;
    }
    
    protected Map<String, Object> args(Map<String, Object> args, String name, Object value)
    {
        args.put(name, value);
        return args;
    }
    
    //
    
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
            this.channel = null;
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
                    RabbitBase.this.init();
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
