package com.intrbiz.queue;

public interface Consumer<T>
{
    String getName();
    
    void take(EventContainer<T> event);
}
