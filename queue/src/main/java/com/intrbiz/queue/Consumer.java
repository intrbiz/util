package com.intrbiz.queue;

import java.util.Set;

import com.intrbiz.queue.name.Queue;
import com.intrbiz.queue.name.RoutingKey;

public interface Consumer<T, K extends RoutingKey> extends AutoCloseable
{
    String name();
    
    Queue queue();
    
    Set<K> bindings();
    
    void addBinding(K key);
    
    DeliveryHandler<T> handler();
    
    @Override
    void close();
}
