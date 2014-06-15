package com.intrbiz.data.cache.memory.local;

import com.intrbiz.data.DataManager.CacheProvider;
import com.intrbiz.data.cache.Cache;

public class LocalMemoryCacheProvider implements CacheProvider
{    
    public LocalMemoryCacheProvider()
    {
        super();
    }

    @Override
    public Cache getCache(String name)
    {
        return new LocalMemoryCache(name);
    }
}
