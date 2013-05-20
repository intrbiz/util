package com.intrbiz.queue;

public interface Queue<T> extends Runnable
{
    /**
     * Create a new Producer to offer events into the queue
     */
    Producer<T> newProducer();
    
    /**
     * Create a new consumer to handle events from the queue
     * @param consumer
     */
    void newConsumer(Consumer<T> consumer);
    
    void shutdown();
    
    boolean isRunning();
}
