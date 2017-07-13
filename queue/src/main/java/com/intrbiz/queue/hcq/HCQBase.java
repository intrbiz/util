package com.intrbiz.queue.hcq;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.intrbiz.hcq.client.HCQClient;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;

public abstract class HCQBase<T> implements AutoCloseable
{
    private static final long RECONNECT_STEP = 500L;
    
    private static final long RECONNECT_MAX = 30_000L;
    
    private static final long RECONNECT_MIN = 0L;
    
    private static Logger logger = Logger.getLogger(HCQBase.class);
    
    protected static final Timer TIMER = new Timer();
    
    protected QueueBrokerPool<HCQClient> broker;
    
    protected QueueEventTranscoder<T> transcoder;
    
    protected volatile boolean closed = false;

    protected HCQClient client;
    
    protected long reconnectDelay = RECONNECT_MIN;
    
    private volatile boolean upAndRunning = false;
    
    public HCQBase(QueueBrokerPool<HCQClient> broker, QueueEventTranscoder<T> transcoder)
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
            this.client = this.broker.connect();
            // setup on disconnect callback
            this.client.onDisconnect(() -> {
                this.upAndRunning = false;
                logger.warn("Disconnected from server, scheduling reconnect");
                this.scheduleReconnect();
            });
            // wait for connect
            this.client.waitConnected();
            // setup this thing
            try
            {
                this.setup();
            }
            catch (Exception e)
            {
                logger.error("Error setting up queue", e);
                this.client.close();
                throw e;
            }
            this.upAndRunning = true;
            // reset the reconnect delay
            this.reconnectDelay = RECONNECT_MIN;
        }
        catch (Exception e)
        {
            this.upAndRunning = false;
            logger.warn("Failed to connect to the HCQ Server, reattempting shortly", e);
            this.scheduleReconnect();
        }
    }
    
    protected void checkUpAndRunning(String message)
    {
        if ((! this.upAndRunning) || this.closed)
            throw new QueueException(message);
    }
    
    protected void scheduleReconnect()
    {
        // compute the reconnect delay
        this.reconnectDelay = Math.min(this.reconnectDelay + RECONNECT_STEP, RECONNECT_MAX);
        // schedule reconnect
        logger.info("Reconnecting in " + this.reconnectDelay + "ms");
        TIMER.schedule(new TimerTask() {
            public void run()
            {
                logger.info("Reconnecting to HCQ server");
                init();
            }
        }, this.reconnectDelay);
    }
    
    /**
     * Configure any queue and exchanges
     */
    protected abstract void setup() throws Exception;
    
    @Override
    public void close()
    {
        if (!this.closed)
        {
            this.closed = true;
            try
            {
                client.close();
            }
            catch (Exception e)
            {
            }
            this.client = null;
        }
    }
}
