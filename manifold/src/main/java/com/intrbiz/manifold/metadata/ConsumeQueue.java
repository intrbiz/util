package com.intrbiz.manifold.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.queue.QueueAdapter;

/**
 * Consume a queue
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConsumeQueue
{
    Class<? extends QueueAdapter> queue();
    
    String consume();
}
