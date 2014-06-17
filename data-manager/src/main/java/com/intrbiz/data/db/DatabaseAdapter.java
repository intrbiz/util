package com.intrbiz.data.db;

import java.util.List;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.intrbiz.data.DataAdapter;
import com.intrbiz.data.DataException;
import com.intrbiz.data.Transaction;
import com.intrbiz.data.cache.Cache;
import com.intrbiz.data.cache.Cache.CacheState;
import com.intrbiz.data.db.DatabaseConnection.DatabaseCall;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * <p>
 * An adapter to a SQL database
 * </p>
 * 
 * <p>
 * Adapters are NOT thread safe, each thread should use its own adapter to access the database.
 * </p>
 * 
 * <p>
 * The simplest way to access the database is:
 * </p>
 * 
 * <pre>
 * <code>
 *   try (BlogAdapter blog = BlogAdapter.connect())
 *   {
 *     // fetch a post
 *     PostDTO post = blog.getPost("test");
 *   }
 * </code>
 * </pre>
 * 
 * <p>
 * Or you can execute a transaction:
 * </p>
 * 
 * <pre>
 * <code>
 *   try (BlogAdapter blog = BlogAdapter.connect())
 *   {
 *     blog.execute(new Transaction() {
 *       public void run() throws Exception
 *       {
 *         // remove a post
 *         blog.removePost("test");
 *         // add a post
 *         blog.setPost(new PostDTO("test"));
 *       }
 *     });
 *   }
 * </code>
 * </pre>
 */
public abstract class DatabaseAdapter implements DataAdapter
{
    protected final DatabaseConnection connection;

    protected int reuseCount = 0;

    protected Cache adapterCache;
    
    protected Logger logger = Logger.getLogger(this.getClass());

    protected DatabaseAdapter(DatabaseConnection connection)
    {
        this.connection = connection;
    }

    protected DatabaseAdapter(DatabaseConnection connection, Cache adapterCache)
    {
        this.connection = connection;
        this.adapterCache = adapterCache;
    }

    /**
     * Get the cache being used by this adapter;
     */
    public Cache getAdapterCache()
    {
        return this.adapterCache;
    }

    /**
     * Mark the reuse of this adapter, preventing immediate closing. This is meant to be used by factories which are implementing ThreadLocal caching.
     */
    public void reuse()
    {
        this.reuseCount++;
    }

    /**
     * Get the reuse count for this adapter
     * 
     * @return the reuse count
     */
    public int getReuseCount()
    {
        return this.reuseCount;
    }

    /**
     * Get the name of the database schema, this will query the name from the database
     */
    @Override
    public final String getName()
    {
        return this.getDatabaseModuleName();
    }

    /**
     * Check if this adapters schema is installed in the database, by checking that the adapter can get the name from the database
     * 
     * @return
     */
    public boolean isInstalled()
    {
        return this.getName() != null;
    }

    /**
     * Get the version of the database schema, this will query the version from the database
     * 
     * @return
     */
    public final String getVersion()
    {
        return this.getDatabaseModuleVersion();
    }

    protected abstract String getDatabaseModuleName();

    protected abstract String getDatabaseModuleVersion();

    @Override
    public final void close()
    {
        /*
         * The reuse count should be incremented by a factory which implements ThreadLocal caching. The connection will only be closed when the outer most close() happens.
         */
        this.reuseCount--;
        if (this.reuseCount <= 0)
        {
            try
            {
                this.beforeClose();
                // close the cache
                if (this.adapterCache != null) this.adapterCache.close();
            }
            finally
            {
                try
                {
                    this.connection.close();
                }
                finally
                {
                    this.afterClose();
                }
            }
        }
    }

    protected void beforeClose()
    {
    }

    protected void afterClose()
    {
    }

    /**
     * Execute the given transaction
     * 
     * @param transaction
     * @throws DataException
     */
    public final void execute(final Transaction transaction) throws DataException
    {
        this.connection.execute(transaction);
    }

    /*
     * Some delegate messages
     */

    public void execute(DatabaseCall<Void> transaction) throws DataException
    {
        connection.execute(transaction);
    }

    public boolean isInTransaction()
    {
        return connection.isInTransaction();
    }

    public void begin() throws DataException
    {
        connection.begin();
    }

    public void rollback() throws DataException
    {
        connection.rollback();
    }

    public void commit() throws DataException
    {
        connection.commit();
    }

    public void end()
    {
        connection.end();
    }

    public <T> T use(DatabaseCall<T> call) throws DataException
    {
        return connection.use(call);
    }

    public <T> T useTimed(Timer timer, DatabaseCall<T> call) throws DataException
    {
        TimerContext tCtx = timer.time();
        try
        {
            return connection.use(call);
        }
        finally
        {
            tCtx.stop();
        }
    }

    // caching
    
    public void cacheOff()
    {
        if (this.adapterCache != null) this.adapterCache.disable();
    }
    
    public void cacheOn()
    {
        if (this.adapterCache != null) this.adapterCache.enable();
    }
    
    public void cacheReadOnly()
    {
        if (this.adapterCache != null) this.adapterCache.readOnly();
    }
    
    public void cacheWriteOnly()
    {
        if (this.adapterCache != null) this.adapterCache.writeOnly();
    }
    
    public CacheState cacheState()
    {
        return this.adapterCache == null ? CacheState.OFF : this.adapterCache.state();
    }
    
    public void cacheState(CacheState state)
    {
        if (this.adapterCache != null) this.adapterCache.state(state);
    }

    public <T> T useCached(String key, Function<T, String> entityKey, DatabaseCall<T> call) throws DataException
    {
        T ret = entityKey == null ? this.adapterCache.get(key) : this.adapterCache.getAndFollow(key);
        if (ret == null)
        {
            // invoke the db call
            ret = this.use(call);
            // cache it
            if (ret != null)
            {
                if (entityKey == null)
                {
                    // put a direct mapping of our key -> result
                    this.adapterCache.put(key, ret);
                }
                else
                {
                    // put a pointer from our key -> pointer
                    this.adapterCache.putPointer(key, ret, entityKey);
                    // put the real value
                    this.adapterCache.put(ret, entityKey);
                }
            }
        }
        return null;
    }

    public <T> T useTimedCached(Timer timer, Meter cacheMiss, String key, Function<T, String> entityKey, DatabaseCall<T> call) throws DataException
    {
        TimerContext tCtx = timer.time();
        try
        {
            // check cache
            T ret = entityKey == null ? this.adapterCache.get(key) : this.adapterCache.getAndFollow(key);
            if (ret == null)
            {
                if (logger.isTraceEnabled()) logger.trace("Cache miss");
                cacheMiss.mark();
                // invoke the db call
                ret = this.use(call);
                // cache it
                if (ret != null)
                {
                    if (entityKey == null)
                    {
                        // put a direct mapping of our key -> result
                        this.adapterCache.put(key, ret);
                    }
                    else
                    {
                        // put a pointer from our key -> pointer
                        this.adapterCache.putPointer(key, ret, entityKey);
                        // put the real value
                        this.adapterCache.put(ret, entityKey);
                    }
                }
            }
            return ret;
        }
        finally
        {
            tCtx.stop();
        }
    }
    
    public <T> List<T> useCachedList(String key, Function<T, String> entityKey, DatabaseCall<List<T>> call) throws DataException
    {
        List<T> ret = this.adapterCache.getAndFollowList(key);
        if (ret == null)
        {
            // invoke the db call
            ret = this.use(call);
            // cache it
            if (ret != null)
            {
                // put a pointer from our key -> pointer
                this.adapterCache.putPointerList(key, ret, entityKey);
                // put the real values
                this.adapterCache.put(ret, entityKey);
            }
        }
        return ret;
    }
    
    public <T> List<T> useTimedCachedList(Timer timer, Meter cacheMiss, String key, Function<T, String> entityKey, DatabaseCall<List<T>> call) throws DataException
    {
        TimerContext tCtx = timer.time();
        try
        {
            // check cache
            List<T> ret = this.adapterCache.getAndFollowList(key);
            if (ret == null)
            {
                if (logger.isTraceEnabled()) logger.trace("Cache miss");
                cacheMiss.mark();
                // invoke the db call
                ret = this.use(call);
                // cache it
                if (ret != null)
                {
                    // put a pointer from our key -> pointer
                    this.adapterCache.putPointerList(key, ret, entityKey);
                    // put the real values
                    this.adapterCache.put(ret, entityKey);
                }
            }
            return ret;
        }
        finally
        {
            tCtx.stop();
        }
    }
}
