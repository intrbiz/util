package com.intrbiz.util.pool.database;

import java.sql.Connection;

import com.intrbiz.configuration.Configurable;

public interface DatabasePool extends Configurable<DatabasePoolConfiguration>
{
    Connection connect() throws Exception;
    
    void close();
    
    
    public static final class Default
    {
        public static DatabasePool create(String url, String username, String password) throws Exception
        {
            DatabasePoolConfiguration cfg = new DatabasePoolConfiguration();
            cfg.setUrl(url);
            cfg.setUsername(username);
            cfg.setPassword(password);
            DatabasePool pool = new DBCPPool();
            pool.configure(cfg);
            return pool;
        }
        
        public static DatabasePool create(String url, String username, String password, String validationSql) throws Exception
        {
            DatabasePoolConfiguration cfg = new DatabasePoolConfiguration();
            cfg.setUrl(url);
            cfg.setUsername(username);
            cfg.setPassword(password);
            cfg.setValidationSql(validationSql);
            DatabasePool pool = new DBCPPool();
            pool.configure(cfg);
            return pool;
        }
        
        public static DatabasePool create(Class<?> driver, String url, String username, String password, String validationSql) throws Exception
        {
            DatabasePoolConfiguration cfg = new DatabasePoolConfiguration();
            cfg.setUrl(url);
            cfg.setUsername(username);
            cfg.setPassword(password);
            cfg.setValidationSql(validationSql);
            DatabasePool pool = new DBCPPool();
            pool.configure(cfg);
            return pool;
        }
        
        public static DatabasePool create(Class<?> driver, String url, String username, String password) throws Exception
        {
            DatabasePoolConfiguration cfg = new DatabasePoolConfiguration();
            cfg.setUrl(url);
            cfg.setUsername(username);
            cfg.setPassword(password);
            DatabasePool pool = new DBCPPool();
            pool.configure(cfg);
            return pool;
        }
    }
}
