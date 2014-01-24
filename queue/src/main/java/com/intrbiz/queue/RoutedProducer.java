package com.intrbiz.queue;

import com.intrbiz.queue.name.RoutingKey;


public interface RoutedProducer<T, K extends RoutingKey> extends AutoCloseable
{
    String exchange();
    
    void publish(K key, T event);
    
    @Override
    void close();
}
