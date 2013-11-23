package com.intrbiz.queue.pgq;

public interface PGQProducer<T>
{
    void put(T event);
}
