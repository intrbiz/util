package com.intrbiz.data.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.codahale.metrics.Timer;
import com.intrbiz.data.DataException;
import com.intrbiz.data.Transaction;
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
     * 
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

    public <T> T useTimed(final Timer timer, final DatabaseCall<T> call) throws DataException
    {
        Timer.Context tCtx = timer.time();
        try
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
        finally
        {
            tCtx.stop();
        }
    }

    /**
     * Execute the given transaction
     * 
     * @param transaction
     * @throws DataException
     */
    public void execute(final Transaction transaction) throws DataException
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

    public void execute(final DatabaseCall<Void> transaction) throws DataException
    {
        this.begin();
        try
        {
            try
            {
                transaction.run(this.borrowConnection());
                this.commit();
            }
            catch (SQLException e)
            {
                throw new DataException(e);
            }

        }
        finally
        {
            this.end();
        }
    }

    /**
     * Internal helper, nothing to see here
     */
    public String getDatabaseModuleName(final String sql)
    {
        try
        {
            return this.use(new DatabaseCall<String>()
            {
                public String run(final Connection with) throws SQLException
                {
                    try (PreparedStatement stmt = with.prepareStatement(sql))
                    {
                        try (ResultSet rs = stmt.executeQuery())
                        {
                            if (rs.next()) return rs.getString(1);
                        }
                    }
                    return null;
                }
            });
        }
        catch (DataException e)
        {
        }
        return null;
    }

    /**
     * Internal helper, nothing to see here
     */
    public String getDatabaseModuleVersion(final String sql)
    {
        return this.getDatabaseModuleName(sql);
    }

    /**
     * Simple callback interface
     * 
     * @param <T>
     */
    @FunctionalInterface()
    public static interface DatabaseCall<T>
    {
        public T run(final Connection with) throws SQLException, DataException;
    }
}
