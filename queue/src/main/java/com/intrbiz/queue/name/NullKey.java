package com.intrbiz.queue.name;

public class NullKey extends RoutingKey
{   
    public NullKey()
    {
        super();
    }

    @Override
    protected String routingKey()
    {
        return "";
    }
}
