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
    
    Future<R> publish(long timeout, T event);
    
    Future<R> publish(long timeout, T event, java.util.function.Consumer<R> onSuccess);
    
    Future<R> publish(long timeout, T event, java.util.function.Consumer<R> onSuccess, java.util.function.Consumer<Exception> onError);
    
    Future<R> publish(K key, T event);
    
    Future<R> publish(K key, long timeout, T event);
    
    Future<R> publish(T event, java.util.function.Consumer<R> onSuccess, java.util.function.Consumer<Exception> onError);
    
    Future<R> publish(T event, java.util.function.Consumer<R> onSuccess);
    
    Future<R> publish(K key, T event, java.util.function.Consumer<R> onSuccess, java.util.function.Consumer<Exception> onError);
    
    Future<R> publish(K key, T event, java.util.function.Consumer<R> onSuccess);
    
    Future<R> publish(K key, long timeout, T event, java.util.function.Consumer<R> onSuccess, java.util.function.Consumer<Exception> onError);
    
    Future<R> publish(K key, long timeout, T event, java.util.function.Consumer<R> onSuccess);
    
    @Override
    void close();
}
