package com.intrbiz.data.db;

import com.intrbiz.data.DataAdapter;
import com.intrbiz.data.DataException;
import com.intrbiz.data.Transaction;

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

    protected DatabaseAdapter(DatabaseConnection connection)
    {
        this.connection = connection;
    }
    
    /**
     * Mark the reuse of this adapter, preventing immediate 
     * closing.  This is meant to be used by factories which 
     * are implementing ThreadLocal caching.
     */
    public void reuse()
    {
        this.reuseCount ++;
    }
    
    /**
     * Get the reuse count for this adapter
     * @return the reuse count
     */
    public int getReuseCount()
    {
        return this.reuseCount;
    }

    /**
     * Get the name of the database schema, 
     * this will query the name from the database
     */
    @Override
    public final String getName()
    {
        return this.getDatabaseModuleName();
    }
    
    /**
     * Check if this adapters schema is installed in the database,
     * by checking that the adapter can get the name from the database
     * @return
     */
    public boolean isInstalled()
    {
        return this.getName() != null;
    }
    
    /**
     * Get the version of the database schema,
     * this will query the version from the database
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
         * The reuse count should be incremented by a factory 
         * which implements ThreadLocal caching.  The connection 
         * will only be closed when the outer most close() happens.
         */
        this.reuseCount--;
        if (this.reuseCount <= 0)
        {
            try
            {
                this.beforeClose();
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
}
