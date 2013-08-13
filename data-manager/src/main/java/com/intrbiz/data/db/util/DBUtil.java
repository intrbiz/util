package com.intrbiz.data.db.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DBUtil
{
    public UUID getUUID(ResultSet rs, int index) throws SQLException
    {
        String value = rs.getString(1);
        return value == null ? null : UUID.fromString(value);
    }
    
    public void setUUID(PreparedStatement stmt, int index, UUID value) throws SQLException
    {
        stmt.setString(index, value == null ? null : value.toString());
    }
}
