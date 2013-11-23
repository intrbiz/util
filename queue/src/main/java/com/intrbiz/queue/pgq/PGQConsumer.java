package com.intrbiz.queue.pgq;


public interface PGQConsumer<T>
{
    String getName();
    
    void take(EventContainer<T> event);
}
