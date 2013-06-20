package com.intrbiz.util.pool.database;

import java.sql.Connection;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.intrbiz.Util;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DBCPPool implements DatabasePool
{
    private DatabasePoolConfiguration cfg;

    protected ConnectionFactory connectionFactory;

    protected GenericObjectPool connectionPool;

    protected KeyedObjectPoolFactory statementPool;

    protected PoolableConnectionFactory poolableConnectionFactory;
    
    protected Counter borrowedConnections;
    
    protected Gauge<Integer> activeConnections;
    
    protected Gauge<Integer> idleConnections;

    @Override
    public void close()
    {
        try
        {
            this.connectionPool.close();
        }
        catch (Exception e)
        {
        }
    }

    @Override
    public Connection connect() throws Exception
    {
        if (this.borrowedConnections != null)
            this.borrowedConnections.inc();
        return (Connection) this.connectionPool.borrowObject();
    }

    @Override
    public void configure(DatabasePoolConfiguration cfg) throws Exception
    {
        this.cfg = cfg;
        // register the driver
        if (! Util.isEmpty(this.cfg.getDriver()))
        {
            Class.forName(this.cfg.getDriver());
        }
        // get url, uname, password
        if (Util.isEmpty(this.cfg.getUrl())) throw new NullPointerException("A database URL must be given to configure the pool");
        if (Util.isEmpty(this.cfg.getUsername())) throw new NullPointerException("A database Username must be given to configure the pool");
        // create the connection factory
        this.connectionFactory = new DriverManagerConnectionFactory(this.cfg.getUrl(), this.cfg.getUsername(), this.cfg.getPassword());
        // create an object pool
        this.connectionPool = new GenericObjectPool();
        // idle settings
        this.connectionPool.setMinIdle(this.cfg.getMinIdle());
        this.connectionPool.setMaxIdle(this.cfg.getMaxIdle());
        // maximum wait 1000ms
        this.connectionPool.setMaxWait(this.cfg.getMaxWait());
        // turn on test on borrow stuff
        this.connectionPool.setTestOnBorrow(this.cfg.isTestOnBorrow());
        this.connectionPool.setTestOnReturn(this.cfg.isTestOnReturn());
        this.connectionPool.setTestWhileIdle(this.cfg.isTestWhileIdle());
        // Set the maximum of connections allowed active at any one time to unlimited
        this.connectionPool.setMaxActive(this.cfg.getMaxActive());
        // create the statement pool
        this.statementPool = new GenericKeyedObjectPoolFactory(null);
        // connections tested before use with 'SELECT 1';
        this.poolableConnectionFactory = new PoolableConnectionFactory(this.connectionFactory, this.connectionPool, this.statementPool, this.cfg.getValidationSql(), false, true);
        // metrics
        String scope = this.cfg.getUsername() + "@" + this.cfg.getUrl();
        this.borrowedConnections = Metrics.newCounter(DatabasePool.class, "borrowed-connections", scope);
        this.activeConnections = Metrics.newGauge(DatabasePool.class, "active-connections", scope, new Gauge<Integer>(){
            @Override
            public Integer value()
            {
                return connectionPool.getNumActive();
            }
        });
        this.idleConnections = Metrics.newGauge(DatabasePool.class, "idle-connections", scope, new Gauge<Integer>(){
            @Override
            public Integer value()
            {
                return connectionPool.getNumIdle();
            }
        }); 
    }

    @Override
    public DatabasePoolConfiguration getConfiguration()
    {
        return this.cfg;
    }
}
