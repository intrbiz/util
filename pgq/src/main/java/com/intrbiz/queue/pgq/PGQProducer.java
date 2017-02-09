package com.intrbiz.queue.pgq;

public interface PGQProducer<T>
{
    default void put(String type, T event) throws PGQueueException
    {
        this.put(type, event, null, null, null, null);
    }
    
    default void put(String type, T event, String extra1) throws PGQueueException
    {
        this.put(type, event, extra1, null, null, null);
    }
    
    default void put(String type, T event, String extra1, String extra2) throws PGQueueException
    {
        this.put(type, event, extra1, extra2, null, null);
    }
    
    default void put(String type, T event, String extra1, String extra2, String extra3) throws PGQueueException
    {
        this.put(type, event, extra1, extra2, extra3, null);
    }
    
    void put(T event) throws PGQueueException;
    
    void put(String type, T event, String extra1, String extra2, String extra3, String extra4) throws PGQueueException;
}
