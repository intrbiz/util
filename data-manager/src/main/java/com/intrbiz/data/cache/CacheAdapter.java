package com.intrbiz.data.cache;

import com.intrbiz.data.DataAdapter;

public abstract class CacheAdapter implements DataAdapter
{
    private final String name;
    
    protected CacheAdapter(String name)
    {
        super();
        this.name = name;
    }
    
    @Override
    public final String getName()
    {
        return this.name;
    }
    
    @Override
    public void close()
    {
    }
}
