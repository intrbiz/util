package com.intrbiz.queue.name;

public final class Exchange
{
    private final String name;

    private final String type;

    private final boolean persistent;

    public Exchange(String name, String type, boolean persistent)
    {
        super();
        this.name = name;
        this.type = type;
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

    public String getType()
    {
        return type;
    }

    public String toString()
    {
        return "Exchange '" + this.name + "', type: '" + this.type + "'";
    }
}
