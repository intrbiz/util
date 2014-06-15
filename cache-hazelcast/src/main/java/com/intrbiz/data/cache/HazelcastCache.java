package com.intrbiz.data.cache;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.IMap;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

public class HazelcastCache implements Cache
{
    private Logger logger = Logger.getLogger(HazelcastCache.class);

    private final String name;

    private final IMap<String, Object> cache;

    private final ConcurrentMap<CacheListener, Object> listeners = new ConcurrentHashMap<CacheListener, Object>();

    private EntryListener<String, Object> listener;

    private String listenerId;

    // metrics

    private final Timer getTimer;

    private final Timer putTimer;

    private final Timer removeTimer;

    private final Timer keySetTimer;
    
    private final Timer containsTimer;

    public HazelcastCache(String name, IMap<String, Object> cache)
    {
        this.name = name;
        this.cache = cache;
        // register a listener
        this.listener = new EntryListener<String, Object>()
        {
            @Override
            public void entryAdded(EntryEvent<String, Object> event)
            {
                this.entryUpdated(event);
            }

            @Override
            public void entryRemoved(EntryEvent<String, Object> event)
            {
                fireRemoval(event.getKey(), event.getValue());
            }

            @Override
            public void entryUpdated(EntryEvent<String, Object> event)
            {
                firePut(event.getKey(), event.getValue());
            }

            @Override
            public void entryEvicted(EntryEvent<String, Object> event)
            {
                this.entryRemoved(event);
            }
        };
        this.listenerId = this.cache.addEntryListener(this.listener, true);
        // metrics
        this.getTimer = Metrics.newTimer(HazelcastCache.class, name + ".get", TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
        this.putTimer = Metrics.newTimer(HazelcastCache.class, name + ".put", TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
        this.removeTimer = Metrics.newTimer(HazelcastCache.class, name + ".remove", TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
        this.keySetTimer = Metrics.newTimer(HazelcastCache.class, name + ".key_set", TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
        this.containsTimer = Metrics.newTimer(HazelcastCache.class, name + ".contains", TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
    }

    @Override
    public String name()
    {
        return this.name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key)
    {
        TimerContext tctx = this.getTimer.time();
        try
        {
            T entry = (T) this.cache.get(key);
            if (logger.isTraceEnabled()) logger.trace("Get: " + key + " => " + entry);
            return entry;
        }
        finally
        {
            tctx.stop();
        }
    }

    @Override
    public <T> void put(String key, T entry)
    {
        TimerContext tctx = this.putTimer.time();
        try
        {
            if (logger.isTraceEnabled()) logger.trace("Put: " + key + " => " + entry);
            this.cache.put(key, entry);
        }
        finally
        {
            tctx.stop();
        }
    }

    @Override
    public void remove(String key)
    {
        TimerContext tctx = this.removeTimer.time();
        try
        {
            if (logger.isTraceEnabled()) logger.trace("Remove: " + key);
            this.cache.remove(key);
        }
        finally
        {
            tctx.stop();
        }
    }
    
    @Override
    public boolean contains(String key)
    {
        TimerContext tctx = this.containsTimer.time();
        try
        {
            return this.cache.containsKey(key);
        }
        finally
        {
            tctx.stop();
        }
    }

    @Override
    public Set<String> keySet(String keyPrefix)
    {
        TimerContext tctx = this.keySetTimer.time();
        try
        {
            return this.cache.keySet(new KeyPrefixPredicate(keyPrefix));
        }
        finally
        {
            tctx.stop();
        }
    }

    @Override
    public void close()
    {
        if (this.listenerId != null) this.cache.removeEntryListener(this.listenerId);
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
