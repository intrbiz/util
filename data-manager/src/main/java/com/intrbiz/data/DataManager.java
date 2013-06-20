package com.intrbiz.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.intrbiz.util.pool.database.DatabasePool;

public final class DataManager
{
    private static final DataManager US = new DataManager();
    
    public static final DataManager getInstance()
    {
        return US;
    }
    
    private DatabasePool defaultPool = null;
    
    private ConcurrentMap<String, DatabasePool> servers = new ConcurrentHashMap<String, DatabasePool>();
    
    private ConcurrentMap<Class<? extends DataAdapter>, DataAdapterFactory<?>> adapters = new ConcurrentHashMap<Class<? extends DataAdapter>, DataAdapterFactory<?>>();
    
    private DataManager()
    {
        super();
    }
    
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
    
    public <T extends DataAdapter> void registerAdapter(Class<T> adapterType, DataAdapterFactory<T> factory)
    {
        this.adapters.put(adapterType, factory);
    }
    
    public <T extends DataAdapter> T adapter(Class<T> adapterType)
    {
        return this.adapter(adapterType, this.defaultServer());
    }
    
    public <T extends DataAdapter> T adapter(Class<T> adapterType,  String server)
    {
        DatabasePool pool = this.server(server);
        // likely to happen
        if (pool == null) throw new RuntimeException("Unknown server: " + server);
        return this.adapter(adapterType, pool);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends DataAdapter> T adapter(Class<T> adapterType,  DatabasePool pool)
    {
        DataAdapterFactory<?> fact = this.adapters.get(adapterType);
        // this should not happen as adapters should register on static {}
        if (fact == null) throw new RuntimeException("Adapter: " + adapterType.getName() + " is not registered!");
        return (T) fact.create(pool);
    }
    
    public static interface DataAdapterFactory<T extends DataAdapter>
    {
        T create(DatabasePool pool);
    }
}