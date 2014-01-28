package com.intrbiz.queue;

import java.io.IOException;

public interface RPCHandler<T>
{
    T handleDevliery(T event) throws IOException;
}
