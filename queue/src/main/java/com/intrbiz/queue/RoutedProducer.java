package com.intrbiz.queue;


public interface RoutedProducer<T> extends AutoCloseable
{
    String exchange();
    
    void publish(String key, T event);
    
    @Override
    void close();
}
