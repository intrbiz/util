package com.intrbiz.data.db.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DBUtil
{
    public static UUID getUUID(ResultSet rs, int index) throws SQLException
    {
        String value = rs.getString(index);
        return value == null ? null : UUID.fromString(value);
    }
    
    public static void setUUID(PreparedStatement stmt, int index, UUID value) throws SQLException
    {
        stmt.setString(index, value == null ? null : value.toString());
    }
    
    public static interface DBGetter<T>
    {
        public T get(ResultSet rs, int index) throws SQLException;
    }
    
    public static <T> T getValue(ResultSet rs, int index, DBGetter<T> getter) throws SQLException
    {
        return getter.get(rs, index);
    }
    
    public static interface DBSetter<T>
    {
        public void set(final PreparedStatement stmt, int index, T value) throws SQLException;
    }
    
    public static <T> void getValue(PreparedStatement stmt, int index, T value, DBSetter<T> setter) throws SQLException
    {
        setter.set(stmt, index, value);
    }
}
