package com.intrbiz.queue;

import com.intrbiz.queue.name.RoutingKey;




public interface Consumer<T> extends AutoCloseable
{
    String name();
    
    boolean requeueOnError();
    
    DeliveryHandler<T> handler();
    
    void addBinding(String binding);
    
    default void addBinding(RoutingKey key)
    {
        this.addBinding(key.toString());
    }
    
    void removeBinding(String binding);
    
    default void removeBinding(RoutingKey key)
    {
        this.removeBinding(key.toString());
    }
    
    @Override
    void close();
}
