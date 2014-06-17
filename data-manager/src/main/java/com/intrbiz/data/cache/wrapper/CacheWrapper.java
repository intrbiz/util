package com.intrbiz.data.cache.wrapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.intrbiz.data.cache.Cache;
import com.intrbiz.data.cache.CacheListener;

public class CacheWrapper implements Cache
{
    private Cache cache;

    private CacheState state = CacheState.ON;

    public CacheWrapper(Cache cache)
    {
        this.cache = cache;
    }
    
    @Override
    public CacheState state()
    {
        return this.state;
    }
    
    @Override
    public void state(CacheState state)
    {
        this.state = state;
    }

    @Override
    public String name()
    {
        return cache.name();
    }

    @Override
    public <T> T get(String key)
    {
        if (this.cache == null || this.state == CacheState.OFF || this.state == CacheState.WRITE_ONLY) return null;
        return cache.get(key);
    }

    @Override
    public <T> T getAndFollow(String key)
    {
        if (this.cache == null || this.state == CacheState.OFF || this.state == CacheState.WRITE_ONLY) return null;
        return cache.getAndFollow(key);
    }

    @Override
    public <T> List<T> getAndFollowList(String key)
    {
        if (this.cache == null || this.state == CacheState.OFF || this.state == CacheState.WRITE_ONLY) return null;
        return cache.getAndFollowList(key);
    }

    @Override
    public <T> T getOrFollow(String key)
    {
        if (this.cache == null || this.state == CacheState.OFF || this.state == CacheState.WRITE_ONLY) return null;
        return cache.getOrFollow(key);
    }

    @Override
    public <T> List<T> getList(String key)
    {
        if (this.cache == null || this.state == CacheState.OFF || this.state == CacheState.WRITE_ONLY) return null;
        return cache.getList(key);
    }

    @Override
    public <T> List<T> getOrFollowList(String key)
    {
        if (this.cache == null || this.state == CacheState.OFF || this.state == CacheState.WRITE_ONLY) return null;
        return cache.getOrFollowList(key);
    }
    
    @Override
    public <T> void put(String key, T value)
    {
        if (this.cache != null && this.state == CacheState.ON && this.state == CacheState.WRITE_ONLY) cache.put(key, value);
    }

    @Override
    public void putPointer(String key, String pointer)
    {
        if (this.cache != null && this.state == CacheState.ON && this.state == CacheState.WRITE_ONLY) cache.putPointer(key, pointer);
    }

    @Override
    public void putPointerList(String key, List<String> pointers)
    {
        if (this.cache != null && this.state == CacheState.ON && this.state == CacheState.WRITE_ONLY) cache.putPointerList(key, pointers);
    }

    @Override
    public <T> void putPointerList(String key, List<T> values, Function<T, String> pointer)
    {
        if (this.cache != null && this.state == CacheState.ON && this.state == CacheState.WRITE_ONLY) cache.putPointerList(key, values, pointer);
    }

    @Override
    public <T> void putPointer(String key, T value, Function<T, String> pointer)
    {
        if (this.cache != null && this.state == CacheState.ON && this.state == CacheState.WRITE_ONLY) cache.putPointer(key, value, pointer);
    }

    @Override
    public <T> void put(T value, Function<T, String> key)
    {
        if (this.cache != null && this.state == CacheState.ON && this.state == CacheState.WRITE_ONLY) cache.put(value, key);
    }

    @Override
    public <T> void put(List<T> values, Function<T, String> key)
    {
        if (this.cache != null && this.state == CacheState.ON && this.state == CacheState.WRITE_ONLY) cache.put(values, key);
    }

    @Override
    public boolean contains(String key)
    {
        if (this.cache == null || this.state == CacheState.OFF || this.state == CacheState.WRITE_ONLY) return false;
        return cache.contains(key);
    }
    
    @Override
    public <T> boolean contains(T value, Function<T, String> key)
    {
        if (this.cache == null || this.state == CacheState.OFF || this.state == CacheState.WRITE_ONLY) return false;
        return cache.contains(value, key);
    }

    @Override
    public void remove(String key)
    {
        if (this.cache != null && this.state == CacheState.ON && this.state == CacheState.WRITE_ONLY) cache.remove(key);
    }
    
    @Override
    public <T> void remove(T value, Function<T, String> key)
    {
        if (this.cache != null && this.state == CacheState.ON && this.state == CacheState.WRITE_ONLY) cache.remove(value, key);
    }

    @Override
    public void removePrefix(String keyPrefix)
    {
        if (this.cache != null && this.state == CacheState.ON && this.state == CacheState.WRITE_ONLY) cache.removePrefix(keyPrefix);
    }

    @Override
    public <T> void removePrefix(T value, Function<T, String> keyPrefix)
    {
        if (this.cache != null && this.state == CacheState.ON && this.state == CacheState.WRITE_ONLY) cache.removePrefix(value, keyPrefix);
    }

    @Override
    public Set<String> keySet(String keyPrefix)
    {
        if (this.cache == null || this.state == CacheState.OFF || this.state == CacheState.WRITE_ONLY) return new HashSet<String>();
        return cache.keySet(keyPrefix);
    }

    @Override
    public void close()
    {
        if (this.cache != null && this.state == CacheState.ON && this.state == CacheState.WRITE_ONLY) cache.close();
    }

    @Override
    public void clear()
    {
        if (this.cache != null && this.state == CacheState.ON && this.state == CacheState.WRITE_ONLY) cache.clear();
    }

    @Override
    public void addListener(CacheListener listener)
    {
        if (this.cache != null) cache.addListener(listener);
    }

    @Override
    public void removeListener(CacheListener listener)
    {
        if (this.cache != null) cache.removeListener(listener);
    }

}
