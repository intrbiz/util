package com.intrbiz.queue.name;

public abstract class RoutingKey
{
    protected abstract String routingKey();

    public final String toString()
    {
        return this.routingKey();
    }
}
