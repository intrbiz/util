package com.intrbiz.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    
    private DatabasePool defaultPool = null;
    
    private ConcurrentMap<String, DatabasePool> servers = new ConcurrentHashMap<String, DatabasePool>();
    
    private ConcurrentMap<Class<? extends DataAdapter>, DataAdapterFactory<?>> dataAdapters = new ConcurrentHashMap<Class<? extends DataAdapter>, DataAdapterFactory<?>>(); 
    
    private ConcurrentMap<Class<? extends DatabaseAdapter>, DatabaseAdapterFactory<?>> databaseAdapters = new ConcurrentHashMap<Class<? extends DatabaseAdapter>, DatabaseAdapterFactory<?>>();
    
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
    
    // factories
    
    public static interface DatabaseAdapterFactory<T extends DatabaseAdapter>
    {
        T create(DatabaseConnection con);
    }
    
    public static interface DataAdapterFactory<T extends DataAdapter>
    {
        T create();
    }
}