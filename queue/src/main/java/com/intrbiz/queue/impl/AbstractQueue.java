package com.intrbiz.queue.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.EventContainer;
import com.intrbiz.queue.Queue;

public abstract class AbstractQueue<T> implements Queue<T>
{
    protected ConcurrentMap<String, Consumer<T>> consumers = new ConcurrentHashMap<String, Consumer<T>>();
    
    public void newConsumer(Consumer<T> consumer)
    {
        this.consumers.put(consumer.getName(), consumer);
    }
    
    protected void consumeEvent(EventContainer<T> event)
    {
        for (Consumer<T> con : consumers.values())
        {
            con.take(event);
        }
    }
    
    public void run()
    {
        // no thread by default
    }
    
    public void shutdown()
    {
    }
    
    public boolean isRunning()
    {
        return false;
    }
}
