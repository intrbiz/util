package com.intrbiz.manifold.model;

import java.io.IOException;

/**
 * Consume specific messages from a queue
 * 
 * Use the @ConsumeQueue annotation to configure the queue to consume from
 *
 * @param <T> Message Type
 */
public interface Messagelet<T>
{
    void setup(MessageletContext context);
    
    void consume(T message) throws IOException;
}
