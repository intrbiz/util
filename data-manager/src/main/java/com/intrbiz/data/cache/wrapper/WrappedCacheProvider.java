package com.intrbiz.data.cache.wrapper;

import com.intrbiz.data.DataManager.CacheProvider;
import com.intrbiz.data.cache.Cache;

public class WrappedCacheProvider implements CacheProvider
{
    private CacheProvider provider;
    
    public WrappedCacheProvider(CacheProvider provider)
    {
        this.provider = provider;
    }

    @Override
    public Cache getCache(String name)
    {
        return new CacheWrapper(this.provider.getCache(name));
    }
}
