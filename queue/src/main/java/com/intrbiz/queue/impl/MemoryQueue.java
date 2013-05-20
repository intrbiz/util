package com.intrbiz.queue.impl;

import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.intrbiz.queue.EventContainer;
import com.intrbiz.queue.Producer;

public final class MemoryQueue<T> extends PollingQueue<T>
{
    private BlockingQueue<EventContainer<T>> queue = new LinkedBlockingQueue<EventContainer<T>>();
    
    public MemoryQueue()
    {
        super(1);
    }

    @Override
    public Producer<T> newProducer()
    {
        return new Producer<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public void put(T event)
            {
                try
                {
                    queue.put(new EventContainer<T>(MemoryQueue.this, new Timestamp(System.currentTimeMillis()), (Class<? extends T>) event.getClass(), event));
                }
                catch (InterruptedException e)
                {
                }
            }
        };
    }

    @Override
    protected boolean consumeEvents()
    {
        try
        {
            EventContainer<T> event = this.queue.take();
            this.consumeEvent(event);
        }
        catch (InterruptedException e)
        {
        }
        return false;
    }
}
