package com.intrbiz.data.db;

import java.sql.Connection;
import java.sql.SQLException;

import com.intrbiz.data.DataException;
import com.intrbiz.util.pool.database.DatabasePool;

public class DatabaseConnection implements AutoCloseable
{
    protected final DatabasePool pool;

    protected Connection transaction;

    public DatabaseConnection(DatabasePool pool)
    {
        super();
        this.pool = pool;
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

    /**
     * Execute something
     * @param call
     * @return
     * @throws DataException
     */
    public <T> T use(final DatabaseCall<T> call) throws DataException
    {
        Connection con = this.borrowConnection();
        try
        {
            return call.run(con);
        }
        catch (SQLException e)
        {
            throw new DataException(e);
        }
        finally
        {
            this.relinquishConnection(con);
        }
    }

    /**
     * Simple callback interface
     *
     * @param <T>
     */
    public static interface DatabaseCall<T>
    {
        public T run(final Connection with) throws SQLException, DataException;
    }
}
