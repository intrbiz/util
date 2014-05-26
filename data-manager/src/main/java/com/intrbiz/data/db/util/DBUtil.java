package com.intrbiz.data.db.util;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    
    //
    
    public static <T extends Enum<T>> T getEnum(ResultSet rs, int index, Class<T> type) throws SQLException
    {
        String value = rs.getString(index);
        return value == null ? null : Enum.valueOf(type, value);
    }
    
    public static <T extends Enum<T>> void setEnum(PreparedStatement stmt, int index, T value) throws SQLException
    {
        stmt.setString(index, value == null ? null : value.toString());
    }
    
    //
    
    @SuppressWarnings("unchecked")
    public static <T> List<T> getArray(ResultSet rs, int index) throws SQLException
    {
        List<T> ret = new LinkedList<T>();
        Array value = rs.getArray(index);
        if (value != null)
        {
            for (T e : (T[]) value.getArray())
            {
                ret.add(e);
            }
        }
        return ret;
    }
    
    @SuppressWarnings("unchecked")
    public static <T, U> List<U> getArray(ResultSet rs, int index, Function<T,U> mapper) throws SQLException
    {
        List<U> ret = new LinkedList<U>();
        Array value = rs.getArray(index);
        if (value != null)
        {
            for (T e : (T[]) value.getArray())
            {
                ret.add(mapper.apply(e));
            }
        }
        return ret;
    }
    
    public static <T> void setArray(PreparedStatement stmt, int index, String sqlType, Collection<T> value) throws SQLException
    {        
        stmt.setArray(index, value == null ? null : stmt.getConnection().createArrayOf(sqlType, value.toArray()));
    }
    
    public static <T,U> void setArray(PreparedStatement stmt, int index, String sqlType, Collection<T> value, Function<T,U> mapper) throws SQLException
    {        
        stmt.setArray(index, value == null ? null : stmt.getConnection().createArrayOf(sqlType, value.stream().map(mapper).collect(Collectors.toList()).toArray()));
    }
    
    //
    
    @FunctionalInterface
    public static interface DBGetter<T>
    {
        public T get(ResultSet rs, int index) throws SQLException;
    }
    
    public static <T> T getValue(ResultSet rs, int index, DBGetter<T> getter) throws SQLException
    {
        return getter.get(rs, index);
    }
    
    @FunctionalInterface
    public static interface DBSetter<T>
    {
        public void set(final PreparedStatement stmt, int index, T value) throws SQLException;
    }
    
    public static <T> void setValue(PreparedStatement stmt, int index, T value, DBSetter<T> setter) throws SQLException
    {
        setter.set(stmt, index, value);
    }
    
    //
    
    public static <D,A> A adaptFromDB(D on, DBTypeAdapter<D,A> adapter)
    {
        return on == null ? null : adapter.fromDB(on);
    }
    
    public static <D,A> D adaptToDB(A on, DBTypeAdapter<D,A> adapter)
    {
        return on == null ? null : adapter.toDB(on);
    }
    
    public static <D,A> List<A> adaptListFromDB(List<D> on, DBTypeAdapter<D,A> adapter)
    {
        if (on == null) return null;
        List<A> ret = new LinkedList<A>();
        for (D e : on)
        {
            ret.add(adapter.fromDB(e));
        }
        return ret;
    }
    
    public static <D, A> List<D> adaptListToDB(List<A> on, DBTypeAdapter<D,A> adapter)
    {
        if (on == null) return null;
        List<D> ret = new LinkedList<D>();
        for (A e : on)
        {
            ret.add(adapter.toDB(e));
        }
        return ret;
    }
}
