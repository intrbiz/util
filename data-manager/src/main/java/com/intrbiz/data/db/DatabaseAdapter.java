package com.intrbiz.data.db;

import java.sql.Connection;
import java.sql.SQLException;

import com.intrbiz.data.DataAdapter;
import com.intrbiz.data.DataException;
import com.intrbiz.data.Transaction;
import com.intrbiz.util.pool.database.DatabasePool;

/**
 * <p>
 * An adapter to a SQL database
 * </p>
 * 
 * <p>
 * Adapters an NOT thread safe, each thread should use its own adapter to access the database.
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
    private final String name;

    private final String version;

    protected final DatabasePool pool;

    protected Connection transaction;

    protected DatabaseAdapter(String name, String version, DatabasePool pool)
    {
        super();
        this.name = name;
        this.version = version;
        this.pool = pool;
    }

    @Override
    public final String getName()
    {
        return name;
    }

    public final String getVersion()
    {
        return version;
    }

    @Override
    public void close()
    {
        // ensure any transaction has been closed
        this.end();
    }

    /* Transaction Management */

    /**
     * Is this adapter currently in a transaction
     * 
     * @return
     */
    public boolean isInTransaction()
    {
        return this.transaction != null;
    }

    /**
     * Start a transaction
     * 
     * This will allocate a single connection to this adapter, until end() is called
     * 
     */
    public void begin() throws DataException
    {
        if (this.transaction == null)
        {
            try
            {
                this.transaction = this.pool.connect();
                // turn off auto commit
                try
                {
                    this.transaction.setAutoCommit(false);
                }
                catch (SQLException e)
                {
                    throw new DataException("Failed to disable AutoCommit", e);
                }
                // logger.trace("Starting transaction");
            }
            catch (Exception e)
            {
                throw new DataException(e);
            }
        }
    }

    /**
     * Rollback the changes of this current transaction
     * 
     * @throws DataException
     *             if an underlying error meant the transaction could not be Rolledback
     */
    public void rollback() throws DataException
    {
        // logger.trace("Rolling back transaction");
        if (this.transaction != null)
        {
            try
            {
                this.transaction.rollback();
            }
            catch (SQLException e)
            {
                throw new DataException("Could not rollback transaction", e);
            }
        }
    }

    /**
     * Commit the changes of this current transaction
     * 
     * @throws DataException
     *             if an underlying error meant the transaction could not be committed
     */
    public void commit() throws DataException
    {
        // logger.trace("Committing transaction");
        if (this.transaction != null)
        {
            try
            {
                this.transaction.commit();
            }
            catch (SQLException e)
            {
                throw new DataException("Could not commit transaction", e);
            }
        }
    }

    /**
     * End the transaction.
     * 
     * The transaction will be rolled back unless commit() has been called. The underlying connection will be released.
     * 
     */
    public void end()
    {
        if (this.transaction != null)
        {
            // force a rollback before closing the transaction
            try
            {
                this.transaction.rollback();
            }
            catch (SQLException e1)
            {
            }
            try
            {
                // ensure we end the transaction
                try
                {
                    this.transaction.setAutoCommit(true);
                }
                catch (SQLException e)
                {
                }
                // return the connection to the pool
                try
                {
                    this.transaction.close();
                }
                catch (Exception e)
                {
                    // eat
                }
            }
            finally
            {
                // reset our state
                this.transaction = null;
                // logger.trace("Finished transaction");
            }
        }
    }

    /**
     * Execute the given transaction
     * 
     * @param transaction
     * @throws DataException
     */
    public void execute(Transaction transaction) throws DataException
    {
        this.begin();
        try
        {
            transaction.run();
            this.commit();
        }
        finally
        {
            this.end();
        }
    }

    /* Internal connection management */

    /**
     * Get the connection to execute SQL against
     */
    protected Connection borrowConnection() throws DataException
    {
        // if we are in a transaction use that connection
        if (this.transaction != null) return this.transaction;
        // otherwise borrow a new connection from the pool
        try
        {
            return this.pool.connect();
        }
        catch (Exception e)
        {
            throw new DataException(e);
        }
    }

    /**
     * Release the connection which was in use
     */
    protected void relinquishConnection(Connection connection)
    {
        // only relinquish the connection if we are NOT in a transaction
        if (this.transaction == null)
        {
            try
            {
                connection.close();
            }
            catch (Exception e)
            {
                // eat
            }
        }
    }
}
