package com.intrbiz.queue;


public interface Producer<T> extends AutoCloseable
{
    String exchange();
    
    void publish(T event);
    
    @Override
    void close();
}
