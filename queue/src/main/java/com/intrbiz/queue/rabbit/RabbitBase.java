package com.intrbiz.queue.rabbit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.rabbitmq.client.Channel;

public abstract class RabbitBase<T> implements AutoCloseable
{
    protected QueueBrokerPool<Channel> broker;
    
    protected QueueEventTranscoder<T> transcoder;
    
    protected volatile boolean closed;

    protected Channel channel;
    
    protected Timer timer = new Timer();
    
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
            // we need to reinit should the connection fail
            this.channel.addShutdownListener((c) -> {
                logger.warn("Lost connection to RabbitMQ, reconnecting in 5s");
                this.scheduleReconnect();
            });
            // setup this thing
            this.setup();
        }
        catch (IOException e)
        {
            logger.warn("Failed to connect to RabbitMQ, reconnecting in 5s", e);
            this.scheduleReconnect();
        }
    }
    
    protected void scheduleReconnect()
    {
        timer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                init();
            }
        }, 5_000L);
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
