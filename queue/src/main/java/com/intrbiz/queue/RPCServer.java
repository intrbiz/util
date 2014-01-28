package com.intrbiz.queue;

import com.intrbiz.queue.name.Queue;

public interface RPCServer<T> extends AutoCloseable
{
    String name();

    Queue requestQueue();

    RPCHandler<T> handler();
    
    @Override
    void close();
}
