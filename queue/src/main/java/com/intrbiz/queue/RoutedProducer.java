package com.intrbiz.queue;

import com.intrbiz.queue.name.RoutingKey;


public interface RoutedProducer<T> extends Producer<T>
{    
    void publish(RoutingKey key, T event);
    
    void publish(RoutingKey key, T event, long ttl);
}
