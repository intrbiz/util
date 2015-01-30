package com.intrbiz.queue;

import com.intrbiz.queue.name.Queue;

public interface RPCServer<T, R> extends AutoCloseable
{
    String name();

    Queue requestQueue();

    RPCHandler<T, R> handler();
    
    @Override
    void close();
}
