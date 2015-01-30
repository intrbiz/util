package com.intrbiz.queue;

import java.io.IOException;

public interface RPCHandler<T, R>
{
    R handleDevliery(T event) throws IOException;
}
