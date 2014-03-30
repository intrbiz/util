package com.intrbiz.queue.name;

public abstract class RoutingKey
{
    protected abstract String routingKey();

    public final String toString()
    {
        return this.routingKey();
    }
    
    public final int hashCode()
    {
        return this.routingKey().hashCode();
    }
    
    public final boolean equals(Object o)
    {
        if (this == o) return true;
        if (o instanceof RoutingKey)
        {
            return this.routingKey().equals(((RoutingKey) o).routingKey());
        }
        return false;
    }
}
