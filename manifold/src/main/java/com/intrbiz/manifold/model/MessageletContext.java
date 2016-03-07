package com.intrbiz.manifold.model;

import java.util.UUID;

import com.intrbiz.queue.QueueAdapter;

public interface MessageletContext
{
    UUID id();
    
    <Q extends QueueAdapter> Q queue();
    
    <Q extends QueueAdapter> Q queue(Class<Q> queueType);
    
    Object[] consumerArguments();
}
