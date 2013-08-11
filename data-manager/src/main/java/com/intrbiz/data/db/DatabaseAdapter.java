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

    @Override
    public final String getName()
    {
        // TODO
        return "";
    }

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
