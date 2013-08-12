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
    protected DatabaseConnection connection;

    protected DatabaseAdapter(DatabaseConnection connection)
    {
        this.connection = connection;
    }

    /**
     * Get the name of the database schema, 
     * this will query the name from the database
     */
    @Override
    public final String getName()
    {
        return this._getDatabaseModuleName();
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
        return this._getDatabaseModuleVersion();
    }
    
    protected abstract String _getDatabaseModuleName();
    
    protected abstract String _getDatabaseModuleVersion();

    @Override
    public final void close()
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
                this.connection = null;
            }
            finally
            {
                this.afterClose();
            }
        }
    }

    protected void beforeClose()
    {
    }

    protected void afterClose()
    {
    }
    
    public void execute(final Transaction transaction) throws DataException
    {
        transaction.run();
    }
}
