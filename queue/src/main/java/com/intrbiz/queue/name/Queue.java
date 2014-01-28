package com.intrbiz.queue.name;

public final class Queue
{
    private final String name;

    private final boolean persistent;

    public Queue(String name, boolean persistent)
    {
        super();
        this.name = name;
        this.persistent = persistent;
    }

    public boolean isPersistent()
    {
        return persistent;
    }

    public String getName()
    {
        return name;
    }
    
    public RoutingKey toKey()
    {
        return new GenericKey(this.name);
    }

    public String toString()
    {
        return "Queue '" + this.name + "'";
    }
}
