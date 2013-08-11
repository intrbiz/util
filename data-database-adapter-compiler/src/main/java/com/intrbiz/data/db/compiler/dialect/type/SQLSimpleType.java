package com.intrbiz.data.db.compiler.dialect.type;

public class SQLSimpleType implements SQLType
{
    private final String sqlType;
    
    private final Class<?>[] javaTypes;
    
    private final String jdbcAccessor;

    public SQLSimpleType(String sqlType, String jdbcAccessor, Class<?>... javaTypes)
    {
        this.sqlType = sqlType;
        this.jdbcAccessor = jdbcAccessor;
        this.javaTypes = javaTypes;
    }

    @Override
    public String getSQLType()
    {
        return this.sqlType;
    }

    @Override
    public Class<?>[] getJavaTypes()
    {
        return this.javaTypes;
    }

    @Override
    public Class<?> getDefaultJavaType()
    {
        return this.javaTypes[0];
    }
    
    public String setBinding(String p, int idx, String value)
    {
        return p + "stmt.set" + this.jdbcAccessor + "(" + idx + ", " + value + ");\r\n";
    }
    
    public String getBinding(int idx)
    {
        return "rs.get" + this.jdbcAccessor + "(" + idx + ")";
    }
}
