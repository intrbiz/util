package com.intrbiz.manifold.model;

/**
 * Consume specific messages from a queue
 *
 * @param <T> Message Type
 */
public interface Messagelet<T>
{
    void setup();
    
    void consume(T message);
}
