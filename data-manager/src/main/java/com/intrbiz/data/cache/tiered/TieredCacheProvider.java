package com.intrbiz.data.cache.tiered;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.intrbiz.data.DataManager.CacheProvider;
import com.intrbiz.data.cache.Cache;
import com.intrbiz.data.cache.memory.shared.SharedMemoryCacheProvider;

public class TieredCacheProvider implements CacheProvider
{
    private final CacheProvider level1;
    
    private final CacheProvider level2;
    
    private final boolean promote;
    
    private final ConcurrentMap<String, TieredCache> caches = new ConcurrentHashMap<String, TieredCache>();
    
    public TieredCacheProvider(CacheProvider level1, CacheProvider level2, boolean promote)
    {
        super();
        this.level1 = level1;
        this.level2 = level2;
        this.promote = promote;
    }
    
    public TieredCacheProvider(CacheProvider level1, CacheProvider level2)
    {
        this(level1, level2, true);
    }
    
    public TieredCacheProvider(CacheProvider level2, boolean promote)
    {
        this(new SharedMemoryCacheProvider(), level2, promote);
    }
    
    public TieredCacheProvider(CacheProvider level2)
    {
        this(level2, true);
    }

    @Override
    public Cache getCache(String name)
    {
        synchronized (this)
        {
            TieredCache cache = this.caches.get(name);
            if (cache == null)
            {
                cache = new TieredCache(name, this.level1.getCache(name), this.level2.getCache(name), this.promote);
                this.caches.put(name, cache);
            }
            return cache;
        }
    }
}
