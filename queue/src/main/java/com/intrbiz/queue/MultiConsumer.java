package com.intrbiz.queue;

import com.intrbiz.queue.name.RoutingKey;

public interface MultiConsumer<T, K extends RoutingKey> extends Consumer<T, K>
{
    String[] names();
}
