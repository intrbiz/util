package com.intrbiz.queue.name;

public class GenericKey extends RoutingKey
{
    private final String key;
    
    public GenericKey(String key)
    {
        super();
        this.key = key;
    }

    @Override
    protected String routingKey()
    {
        return this.key;
    }
}
