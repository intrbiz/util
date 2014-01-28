package com.intrbiz.queue;

import com.intrbiz.queue.name.Queue;

public interface Consumer<T> extends AutoCloseable
{
    String name();
    
    Queue queue();
    
    DeliveryHandler<T> handler();
    
    @Override
    void close();
}
