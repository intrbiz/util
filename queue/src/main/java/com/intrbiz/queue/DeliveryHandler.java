package com.intrbiz.queue;

import java.io.IOException;
import java.util.Map;

@FunctionalInterface
public interface DeliveryHandler<T>
{    
    void handleDevliery(Map<String, Object> headers, T event) throws IOException;
}
