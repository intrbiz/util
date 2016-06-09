package com.intrbiz.util.pool.database;

import java.sql.Connection;

import com.intrbiz.configuration.Configurable;

public interface DatabasePool extends Configurable<DatabasePoolConfiguration>
{
    Connection connect() throws Exception;
    
    void close();
    
    public static final class Default
    {
        private DatabasePoolConfiguration config = new DatabasePoolConfiguration();
        
        private Default()
        {
            super();
        }
        
        public static final Default with()
        {
            return new Default();
        }
        
        public Default url(String url)
        {
            this.config.setUrl(url);
            return this;
        }
        
        public Default username(String username)
        {
            this.config.setUsername(username);
            return this;
        }
        
        public Default password(String password)
        {
            this.config.setPassword(password);
            return this;
        }
        
        public Default validationSQL(String validationSql)
        {
            this.config.setValidationSql(validationSql);
            return this;
        }
        
        public Default driver(String driver)
        {
            this.config.setDriver(driver);
            return this;
        }
        
        public Default maxActive(int maxActive)
        {
            this.config.setMaxActive(maxActive);
            return this;
        }
        
        public Default setMinIdle(int minIdle)
        {
            this.config.setMinIdle(minIdle);
            return this;
        }

        public Default setMaxIdle(int maxIdle)
        {
            this.config.setMaxIdle(maxIdle);
            return this;
        }

        public Default setMaxWait(long maxWait)
        {
            this.config.setMaxWait(maxWait);
            return this;
        }

        public Default setTestOnBorrow(boolean testOnBorrow)
        {
            this.config.setTestOnBorrow(testOnBorrow);
            return this;
        }

        public Default setTestOnReturn(boolean testOnReturn)
        {
            this.config.setTestOnReturn(testOnReturn);
            return this;
        }

        public Default setTestWhileIdle(boolean testWhileIdle)
        {
            this.config.setTestWhileIdle(testWhileIdle);
            return this;
        }

        public Default postgresql()
        {
            this.config.setDriver(Driver.POSTGRESQL);
            this.config.setValidationSql(ValidationSQL.POSTGRESQL);
            return this;
        }
        
        public DatabasePoolConfiguration buildConfig()
        {
            return config;
        }
        
        public DatabasePool build() throws Exception
        {
            DatabasePool pool = new DBCPPool();
            pool.configure(this.buildConfig());
            return pool;
        }
    }
    
    public static final class Driver
    {
        public static final String POSTGRESQL = "org.postgresql.Driver";
    }
    
    public static final class ValidationSQL
    {
        public static final String POSTGRESQL = "SELECT 1";
    }
}
