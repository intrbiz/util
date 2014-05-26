package com.intrbiz.data.db.util;

/**
 * Map between database types and application types
 *
 * @param <DBType>
 * @param <AppType>
 */
public interface DBTypeAdapter<DBType, AppType>
{    
    DBType toDB(AppType value);
    
    AppType fromDB(DBType value);
}
