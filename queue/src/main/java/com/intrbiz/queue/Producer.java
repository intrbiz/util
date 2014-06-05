package com.intrbiz.queue;



public interface Producer<T> extends AutoCloseable
{   
    void publish(T event);
    
    void publish(T event, long ttl);
    
    @Override
    void close();
}
