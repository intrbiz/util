package com.intrbiz.queue;

public interface Consumer<T> extends AutoCloseable
{
    String name();
    
    String queueName();
    
    DeliveryHandler<T> handler();
    
    @Override
    void close();
}
