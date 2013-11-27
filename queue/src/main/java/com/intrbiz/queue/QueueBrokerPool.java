package com.intrbiz.queue;

/**
 * A pool / factory of broker connections
 */
public interface QueueBrokerPool<T>
{
    T connect();
    
    void close();
}
