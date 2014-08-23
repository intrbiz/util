package com.intrbiz.data.cache.memory.local;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.data.cache.Cache;
import com.intrbiz.data.cache.CacheListener;

/**
 * A non-shared Level 1 cache
 */
public class LocalMemoryCache implements Cache
{
    private Logger logger = Logger.getLogger(LocalMemoryCache.class);
    
    private final String name;

    private final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<String, Object>();

    private final ConcurrentMap<CacheListener, Object> listeners = new ConcurrentHashMap<CacheListener, Object>();

    public LocalMemoryCache(String name)
    {
        super();
        this.name = name;
    }

    public String name()
    {
        return this.name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key)
    {
        T entry = (T) this.cache.get(key);
        if (logger.isTraceEnabled()) logger.trace("Get: " + key + " => " + entry);
        return entry;
    }

    @Override
    public <T> void put(String key, T entry)
    {
        if (logger.isTraceEnabled()) logger.trace("Put: " + key + " => " + entry);
        this.cache.put(key, entry);
        this.firePut(key, entry);
    }

    @Override
    public void remove(String key)
    {
        if (logger.isTraceEnabled()) logger.trace("Remove: " + key);
        Object entry = this.cache.remove(key);
        this.fireRemoval(key, entry);
    }
    
    @Override
    public boolean contains(String key)
    {
        return this.cache.containsKey(key);
    }

    @Override
    public Set<String> keySet(String keyPrefix)
    {
        return this.cache.keySet().stream().filter((k) -> {
            return k.startsWith(keyPrefix);
        }).collect(Collectors.toSet());
    }

    @Override
    public void close()
    {
        this.cache.clear();
        this.listeners.clear();
    }
    
    @Override
    public void clear()
    {
        this.cache.clear();
    }

    @Override
    public void addListener(CacheListener listener)
    {
        this.listeners.put(listener, this);
    }
    
    @Override
    public void removeListener(CacheListener listener)
    {
        this.listeners.remove(listener);
    }
    
    protected void fireRemoval(String key, Object entry)
    {
        for (CacheListener listener : this.listeners.keySet())
        {
            listener.onRemove(key, entry);
        }
    }
    
    protected void firePut(String key, Object entry)
    {
        for (CacheListener listener : this.listeners.keySet())
        {
            listener.onPut(key, entry);
        }
    }
}
