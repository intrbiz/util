package com.intrbiz.queue;

import java.io.IOException;

public interface DeliveryHandler<T>
{
    void handleDevliery(T event) throws IOException;
}
