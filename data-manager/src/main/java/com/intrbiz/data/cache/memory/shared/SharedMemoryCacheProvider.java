package com.intrbiz.data.cache.memory.shared;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.intrbiz.data.DataManager.CacheProvider;
import com.intrbiz.data.cache.Cache;

public class SharedMemoryCacheProvider implements CacheProvider
{
    private final ConcurrentMap<String, SharedMemoryCache> caches = new ConcurrentHashMap<String, SharedMemoryCache>();
    
    public SharedMemoryCacheProvider()
    {
        super();
    }

    @Override
    public Cache getCache(String name)
    {
        synchronized (this)
        {
            SharedMemoryCache cache = this.caches.get(name);
            if (cache == null)
            {
                cache = new SharedMemoryCache(name);
                this.caches.put(name, cache);
            }
            return cache;
        }
    }
}
