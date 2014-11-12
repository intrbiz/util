package com.intrbiz.queue;

import java.util.concurrent.Future;

import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.Queue;
import com.intrbiz.queue.name.RoutingKey;

public interface RPCClient<T, K extends RoutingKey> extends AutoCloseable
{
    Exchange exchange();
    
    Queue replyQueue();
    
    Future<T> publish(T event);
    
    Future<T> publish(K key, T event);
    
    Future<T> publish(K key, long timeout, T event);
    
    @Override
    void close();
}
