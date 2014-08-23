package com.intrbiz.data.cache.memory.shared;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.data.cache.Cache;
import com.intrbiz.data.cache.CacheListener;

/**
 * A shared in memory cache suitable for Level 1 or Level 2 use
 */
public class SharedMemoryCache implements Cache
{
    private Logger logger = Logger.getLogger(SharedMemoryCache.class);
    
    private final String name;
    
    private final ConcurrentMap<String, WeakEntry> cache = new ConcurrentHashMap<String, WeakEntry>();
    
    private final ConcurrentMap<CacheListener, Object> listeners = new ConcurrentHashMap<CacheListener, Object>();

    private final ReferenceQueue<Object> queue = new ReferenceQueue<Object>();
    
    private volatile boolean processQueue = true;
    
    private final Thread thread;
    
    public SharedMemoryCache(String name)
    {
        super();
        this.name = name;
        this.thread = new Thread(() -> {
            WeakEntry e;
            while (this.processQueue)
            {
                try
                {
                    while ((e = (WeakEntry) this.queue.remove(1000)) != null)
                    {
                        if (logger.isTraceEnabled()) logger.trace("Evicting key " + e.getKey() + " due to weak reference being collected.");
                        this.remove(e.getKey());
                    }
                }
                catch (InterruptedException ex)
                {
                }
            }
        }, "ReferenceCleaner::" + name);
        this.thread.setDaemon(true);
        this.thread.start();
    }
    
    @Override
    public void finalize()
    {
        this.processQueue = false;
    }
    
    public String name()
    {
        return this.name;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key)
    {
        WeakEntry weak = this.cache.get(key);
        T entry = (T) (weak == null ? null : weak.get());
        if (logger.isTraceEnabled()) logger.trace("Get: " + key + " => " + entry);
        return entry;
    }

    @Override
    public <T> void put(String key, T entry)
    {
        if (logger.isTraceEnabled()) logger.trace("Put: " + key + " => " + entry);
        this.cache.put(key, new WeakEntry(key, entry));
        this.firePut(key, entry);
    }

    @Override
    public void remove(String key)
    {
        if (logger.isTraceEnabled()) logger.trace("Remove: " + key);
        WeakEntry weak = this.cache.remove(key);
        this.fireRemoval(key, weak == null ? null : weak.get());
    }
    
    @Override
    public boolean contains(String key)
    {
        return this.cache.containsKey(key);
    }

    @Override
    public Set<String> keySet(String keyPrefix)
    {
        return this.cache.keySet().stream().filter((k) -> {return k.startsWith(keyPrefix);}).collect(Collectors.toSet());
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
    
    @Override
    public void clear()
    {
        this.cache.clear();
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
    
    protected class WeakEntry extends WeakReference<Object>
    {
        private final String key;

        public WeakEntry(String key, Object value)
        {
            super(value, queue);
            this.key = key;
        }

        public String getKey()
        {
            return key;
        }
        
        public String toString()
        {
            return "WeakEntry(" + this.get() + ")";
        }
    }
}
