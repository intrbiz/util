package com.intrbiz.queue;

import com.intrbiz.queue.name.RoutingKey;


public interface RoutedProducer<T, K extends RoutingKey> extends Producer<T>
{    
    void publish(K key, T event);
    
    void publish(K key, T event, long ttl);
}
