package com.intrbiz.queue;

import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.RoutingKey;


public interface RoutedProducer<T, K extends RoutingKey> extends AutoCloseable
{
    Exchange exchange();
    
    void publish(K key, T event);
    
    @Override
    void close();
}
