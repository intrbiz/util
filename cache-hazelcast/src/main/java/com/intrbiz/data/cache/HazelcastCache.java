package com.intrbiz.data.cache;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.codahale.metrics.Timer;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionOptions.TransactionType;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;

public final class HazelcastCache implements Cache
{
    private Logger logger = Logger.getLogger(HazelcastCache.class);

    private final String name;
    
    private final HazelcastInstance hazelcastInstance;
    
    private final IMap<String, Object> cache;

    private final ConcurrentMap<CacheListener, Object> listeners = new ConcurrentHashMap<CacheListener, Object>();

    private EntryListener<String, Object> listener;

    private String listenerId;
    
    // transaction support
    
    private TransactionContext transaction;
    
    private TransactionalMap<String, Object> transactionCache;

    // metrics

    private final Timer getTimer;

    private final Timer putTimer;

    private final Timer removeTimer;

    private final Timer keySetTimer;
    
    private final Timer containsTimer;

    public HazelcastCache(String name, HazelcastInstance hazelcastInstance)
    {
        this.name = name;
        this.hazelcastInstance = hazelcastInstance;
        // the map
        this.cache = this.hazelcastInstance.getMap(HazelcastCacheProvider.MAP_PREFIX + this.name);
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

            @Override
            public void mapEvicted(MapEvent event)
            {
                // nothing to do yet
            }

            @Override
            public void mapCleared(MapEvent event)
            {
                // nothing to do yet
            }
        };
        this.listenerId = this.cache.addEntryListener(this.listener, true);
        // the soource to register metrics on
        IntelligenceSource source = Witchcraft.get().source("com.intrbiz.cache.hazelcast");
        // metrics
        this.getTimer      = source.getRegistry().timer(Witchcraft.scoped(HazelcastCache.class, "get",      name));
        this.putTimer      = source.getRegistry().timer(Witchcraft.scoped(HazelcastCache.class, "put",      name));
        this.removeTimer   = source.getRegistry().timer(Witchcraft.scoped(HazelcastCache.class, "remove",   name));
        this.keySetTimer   = source.getRegistry().timer(Witchcraft.scoped(HazelcastCache.class, "key_set",  name));
        this.containsTimer = source.getRegistry().timer(Witchcraft.scoped(HazelcastCache.class, "contains", name));
    }

    @Override
    public String name()
    {
        return this.name;
    }
    
    @Override
    public boolean isTransactional()
    {
        return true;
    }
        
    @Override
    public void begin()
    {
        // start a transaction
        if (this.transaction != null)
        {
            // the transaction context
            this.transaction = this.hazelcastInstance.newTransactionContext(new TransactionOptions().setTransactionType(TransactionType.LOCAL));
            this.transaction.beginTransaction();
            // get the map
            this.transactionCache = this.transaction.getMap(HazelcastCacheProvider.MAP_PREFIX + this.name);
        }
    }

    @Override
    public void commit()
    {
        if (this.transaction != null)
        {
            this.transaction.commitTransaction();
        }
    }

    @Override
    public void rollback()
    {
        if (this.transaction != null)
        {
            this.transaction.rollbackTransaction();
        }
    }

    @Override
    public void end()
    {
        this.rollback();
        this.transaction = null;
        this.transactionCache = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key)
    {
        Timer.Context tctx = this.getTimer.time();
        try
        {
            T entry = null;
            if (this.transactionCache == null)
            {
                entry = (T) this.cache.get(key);
            }
            else
            {
                entry = (T) this.transactionCache.get(key);
            }
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
        Timer.Context tctx = this.putTimer.time();
        try
        {
            if (logger.isTraceEnabled()) logger.trace("Put: " + key + " => " + entry);
            if (this.transactionCache == null)
            {
                this.cache.put(key, entry);
            }
            else
            {
                this.transactionCache.put(key, entry);
            }
        }
        finally
        {
            tctx.stop();
        }
    }

    @Override
    public void remove(String key)
    {
        Timer.Context tctx = this.removeTimer.time();
        try
        {
            if (logger.isTraceEnabled()) logger.trace("Remove: " + key);
            if (this.transactionCache == null)
            {
                this.cache.remove(key);
            }
            else
            {
                this.transactionCache.remove(key);
            }
        }
        finally
        {
            tctx.stop();
        }
    }
    
    @Override
    public boolean contains(String key)
    {
        Timer.Context tctx = this.containsTimer.time();
        try
        {
            if (this.transactionCache == null)
            {
                return this.cache.containsKey(key);
            }
            else
            {
                return this.transactionCache.containsKey(key);
            }
        }
        finally
        {
            tctx.stop();
        }
    }

    @Override
    public Set<String> keySet(String keyPrefix)
    {
        Timer.Context tctx = this.keySetTimer.time();
        try
        {
            if (this.transactionCache == null)
            {
                return this.cache.keySet(new KeyPrefixPredicate(keyPrefix));
            }
            else
            {
                return this.transactionCache.keySet(new KeyPrefixPredicate(keyPrefix));   
            }
        }
        finally
        {
            tctx.stop();
        }
    }
    
    public void clear()
    {
        // cannot be transactional :(
        this.cache.clear();
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
