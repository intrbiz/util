package com.intrbiz.queue;

/**
 * Adapters to message queues
 */
public abstract class QueueAdapter implements AutoCloseable
{
    public abstract String getName();
    
    @Override
    public abstract void close();
}
