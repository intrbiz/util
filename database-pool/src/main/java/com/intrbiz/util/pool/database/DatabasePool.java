package com.intrbiz.util.pool.database;

import java.sql.Connection;

import com.intrbiz.configuration.Configurable;

public interface DatabasePool extends Configurable<DatabasePoolConfiguration>
{
    Connection connect() throws Exception;
    
    void close();
    
    public static final class Default
    {
        private String driver;
        
        private String url;
        
        private String username;
        
        private String password;
        
        private String validationSql;
        
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
            this.url = url;
            return this;
        }
        
        public Default username(String username)
        {
            this.username = username;
            return this;
        }
        
        public Default password(String password)
        {
            this.password = password;
            return this;
        }
        
        public Default validationSQL(String validationSql)
        {
            this.validationSql = validationSql;
            return this;
        }
        
        public Default driver(String driver)
        {
            this.driver = driver;
            return this;
        }
        
        public Default postgresql()
        {
            this.driver = Driver.POSTGRESQL;
            this.validationSql = ValidationSQL.POSTGRESQL;
            return this;
        }
        
        public DatabasePoolConfiguration buildConfig()
        {
            DatabasePoolConfiguration config = new DatabasePoolConfiguration();
            config.setDriver(this.driver);
            config.setValidationSql(this.validationSql);
            config.setUrl(this.url);
            config.setUsername(this.username);
            config.setPassword(this.password);
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
