package com.intrbiz.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.intrbiz.data.cache.Cache;
import com.intrbiz.data.cache.memory.shared.SharedMemoryCacheProvider;
import com.intrbiz.data.db.DatabaseAdapter;
import com.intrbiz.data.db.DatabaseConnection;
import com.intrbiz.util.pool.database.DatabasePool;

public final class DataManager
{
    private static final DataManager US = new DataManager();
    
    public static final DataManager getInstance()
    {
        return US;
    }
    
    public static final DataManager get()
    {
        return US;
    }
    
    // pools
    
    private DatabasePool defaultPool = null;
    
    private ConcurrentMap<String, DatabasePool> servers = new ConcurrentHashMap<String, DatabasePool>();
    
    // cache providers
    
    private CacheProvider defaultCache = new SharedMemoryCacheProvider();
    
    private ConcurrentMap<String, CacheProvider> caches = new ConcurrentHashMap<String, CacheProvider>();
    
    // adapters
    
    private ConcurrentMap<Class<? extends DataAdapter>, DataAdapterFactory<?>> dataAdapters = new ConcurrentHashMap<Class<? extends DataAdapter>, DataAdapterFactory<?>>(); 
    
    private ConcurrentMap<Class<? extends DatabaseAdapter>, DatabaseAdapterFactory<?>> databaseAdapters = new ConcurrentHashMap<Class<? extends DatabaseAdapter>, DatabaseAdapterFactory<?>>();
    
    private DataManager()
    {
        super();
    }
    
    // pools
    
    public void registerDefaultServer(DatabasePool pool)
    {
        this.defaultPool = pool;
    }
    
    public void registerServer(String name, DatabasePool pool)
    {
        this.servers.put(name, pool);
    }
    
    public DatabasePool defaultServer()
    {
        return this.defaultPool;
    }
    
    public DatabasePool server(String name)
    {
        return this.servers.get(name);
    }
    
    // connections
    
    public DatabaseConnection connect()
    {
        return new DatabaseConnection(this.defaultServer());
    }
    
    public DatabaseConnection connect(String server)
    {
        return new DatabaseConnection(this.server(server));
    }
    
    // database adapters
    
    public <T extends DatabaseAdapter> void registerDatabaseAdapter(Class<T> type, DatabaseAdapterFactory<T> factory)
    {
        this.databaseAdapters.put(type, factory);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends DatabaseAdapter> T databaseAdapter(Class<T> type, DatabaseConnection connection)
    {
        return (T) this.databaseAdapters.get(type).create(connection);
    }
    
    public <T extends DatabaseAdapter> T databaseAdapter(Class<T> type)
    {
        return this.databaseAdapter(type, this.connect());
    }
    
    public <T extends DatabaseAdapter> T databaseAdapter(Class<T> type, String server)
    {
        return this.databaseAdapter(type, this.connect(server));
    }
    
    // data adapters
    
    public <T extends DataAdapter> void registerDataAdapter(Class<T> type, DataAdapterFactory<T> factory)
    {
        this.dataAdapters.put(type, factory);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends DataAdapter> T dataAdapter(Class<T> type)
    {
        return (T) this.dataAdapters.get(type).create();
    }
    
    // cache providers
    
    public void registerDefaultCacheProvider(CacheProvider provider)
    {
        this.defaultCache = provider;
    }
    
    public CacheProvider defaultCacheProvider()
    {
        return this.defaultCache;
    }
    
    public void registerCacheProvider(String name, CacheProvider provider)
    {
        this.caches.put(name, provider);
    }
    
    public CacheProvider cacheProvider(String name)
    {
        return this.caches.get(name);
    }
    
    public Cache cache(String name)
    {
        return this.defaultCache == null ? null : (Cache) this.defaultCache.getCache(name);
    }
    
    public Cache cache(String provider, String name)
    {
        CacheProvider cp = this.caches.get(provider);
        return cp == null ? null : (Cache) cp.getCache(name);
    }
    
    // factories
    
    @FunctionalInterface
    public static interface DatabaseAdapterFactory<T extends DatabaseAdapter>
    {
        T create(DatabaseConnection con);
    }
    
    @FunctionalInterface
    public static interface DataAdapterFactory<T extends DataAdapter>
    {
        T create();
    }
    
    @FunctionalInterface
    public static interface CacheProvider
    {
        Cache getCache(String name);
    }
}