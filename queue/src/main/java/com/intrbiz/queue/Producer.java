package com.intrbiz.queue;

public interface Producer<T>
{
    void put(T event);
}
