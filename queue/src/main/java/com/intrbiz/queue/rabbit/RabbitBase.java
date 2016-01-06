package com.intrbiz.queue.rabbit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Recoverable;

public abstract class RabbitBase<T> implements AutoCloseable
{
    protected QueueBrokerPool<Channel> broker;
    
    protected QueueEventTranscoder<T> transcoder;
    
    protected volatile boolean closed;

    protected Channel channel;
    
    private Logger logger = Logger.getLogger(RabbitBase.class);
    
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
            // log recovery events
            ((Recoverable) this.channel).addRecoveryListener((r) -> {
                logger.warn("Lost connection to RabbitMQ lost, auto-recovery complete");
            });
            // setup this thing
            this.setup();
        }
        catch (IOException e)
        {
            logger.warn("Failed to connect to RabbitMQ", e);
            throw new QueueException("Failed to connect to RabbitMQ, is it up?", e);
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
}
