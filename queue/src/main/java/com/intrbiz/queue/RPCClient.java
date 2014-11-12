package com.intrbiz.queue;

import java.util.concurrent.Future;

import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.Queue;
import com.intrbiz.queue.name.RoutingKey;

public interface RPCClient<T, R, K extends RoutingKey> extends AutoCloseable
{
    Exchange exchange();
    
    Queue replyQueue();
    
    Future<R> publish(T event);
    
    Future<R> publish(K key, T event);
    
    Future<R> publish(K key, long timeout, T event);
    
    @Override
    void close();
}
