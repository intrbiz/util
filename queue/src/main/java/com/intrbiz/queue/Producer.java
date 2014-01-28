package com.intrbiz.queue;

import com.intrbiz.queue.name.Exchange;


public interface Producer<T> extends AutoCloseable
{
    Exchange exchange();
    
    void publish(T event);
    
    @Override
    void close();
}
