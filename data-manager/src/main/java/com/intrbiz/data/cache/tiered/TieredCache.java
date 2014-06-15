package com.intrbiz.data.cache.tiered;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.intrbiz.data.cache.Cache;
import com.intrbiz.data.cache.CacheListener;

/**
 * A tiered cache which will pull from a level 1 cache and resort to a level 2 cache if needed, with optional promotion on get.
 */
public class TieredCache implements Cache
{
    private Logger logger = Logger.getLogger(TieredCache.class);

    private final String name;

    private final Cache level1;

    private final Cache level2;

    private final boolean promote;

    private final ConcurrentMap<CacheListener, Object> listeners = new ConcurrentHashMap<CacheListener, Object>();

    private CacheListener listener;

    public TieredCache(String name, Cache level1, Cache level2, boolean promote)
    {
        super();
        this.name = name;
        this.level1 = level1;
        this.level2 = level2;
        this.promote = promote;
        // setup listener on the second level cache
        this.listener = new CacheListener()
        {
            @Override
            public void onRemove(String key, Object entry)
            {
                // remove the entry from the level 1 cache
                if (logger.isTraceEnabled()) logger.trace("Key " + key + " has been removed in the level 2 cache, removing from level 1");
                level1.remove(key);
                // fire our listeners
                fireRemoval(key, entry);
            }

            @Override
            public void onPut(String key, Object entry)
            {
                // promote the key to level1 or remove the key from level 1
                if (promote && entry != null && level1.contains(key))
                {
                    if (logger.isTraceEnabled()) logger.trace("Key " + key + " has been updated in the level 2 cache, promoting to level 1");
                    level1.put(key, entry);
                }
                else
                {
                    if (logger.isTraceEnabled()) logger.trace("Key " + key + " has been updated in the level 2 cache, removing");
                    level1.remove(key);
                }
                // fire our listeners
                firePut(key, entry);
            }
        };
        this.level2.addListener(this.listener);
    }

    @Override
    public String name()
    {
        return this.name;
    }

    public boolean isPromote()
    {
        return promote;
    }

    @Override
    public <T> T get(String key)
    {
        T entry = this.level1.get(key);
        if (entry == null)
        {
            if (logger.isTraceEnabled()) logger.trace("Cache miss in level 1, trying level 2");
            entry = this.level2.get(key);
            // promote?
            if (entry != null && this.promote)
            {
                if (logger.isTraceEnabled()) logger.trace("Promoting key " + key + " from level 2 cache into level 1 cache");
                this.level1.put(key, entry);
            }
        }
        if (logger.isTraceEnabled()) logger.trace("Get: " + key + " => " + entry);
        return entry;
    }

    @Override
    public <T> void put(String key, T entry)
    {
        if (logger.isTraceEnabled()) logger.trace("Put: " + key + " => " + entry);
        this.level1.put(key, entry);
        this.level2.put(key, entry);
    }

    @Override
    public void remove(String key)
    {
        if (logger.isTraceEnabled()) logger.trace("Remove: " + key);
        this.level1.remove(key);
        this.level2.remove(key);
    }

    @Override
    public boolean contains(String key)
    {
        return this.level1.contains(key) || this.level2.contains(key);
    }

    @Override
    public Set<String> keySet(String keyPrefix)
    {
        Set<String> keys = new HashSet<String>();
        keys.addAll(this.level1.keySet(keyPrefix));
        keys.addAll(this.level2.keySet(keyPrefix));
        return keys;
    }

    @Override
    public void close()
    {
        // this cache is shared, do not close it
    }

    public void clear()
    {
        this.level1.clear();
        this.level2.clear();
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
