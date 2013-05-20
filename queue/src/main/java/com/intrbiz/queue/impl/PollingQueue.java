package com.intrbiz.queue.impl;

public abstract class PollingQueue<T> extends AbstractQueue<T>
{
    protected boolean run = false;
    
    protected long pollDelay;
    
    protected PollingQueue(long pollDelay)
    {
        super();
        this.pollDelay = pollDelay;
    }
    
    public void run()
    {
        this.run = true;
        while (this.run)
        {
            if (this.consumeEvents())
            {
                // sleep
                synchronized (this)
                {
                    try
                    {
                        this.wait(this.pollDelay);
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
            }
        }
    }
    
    /**
     * The implementation should implement this 
     * method to consume events.
     * 
     * @return if true the thread will wait before consuming more events
     */
    protected abstract boolean consumeEvents();
    
    
    public void shutdown()
    {
        this.run = false;
        // wake up if we are sleeping
        synchronized (this)
        {
            this.notifyAll();
        }
    }
    
    public boolean isRunning()
    {
        return this.run;
    }
}
