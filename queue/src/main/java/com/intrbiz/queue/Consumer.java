package com.intrbiz.queue;

import java.util.Set;

import com.intrbiz.queue.name.RoutingKey;




public interface Consumer<T, K extends RoutingKey> extends AutoCloseable
{
    String name();
    
    boolean requeueOnError();
    
    DeliveryHandler<T> handler();
    
    void addBinding(K binding);
    
    void removeBinding(K binding);
    
    Set<K> getBindings();
    
    @Override
    void close();
}
