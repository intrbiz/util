package com.intrbiz.queue;

import java.io.IOException;

@FunctionalInterface
public interface DeliveryHandler<T>
{
    void handleDevliery(T event) throws IOException;
}
