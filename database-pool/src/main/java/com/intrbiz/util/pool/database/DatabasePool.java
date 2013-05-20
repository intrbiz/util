package com.intrbiz.util.pool.database;

import java.sql.Connection;

import com.intrbiz.configuration.Configurable;

public interface DatabasePool extends Configurable<DatabasePoolConfiguration>
{
    Connection connect() throws Exception;
    
    void close();
}
