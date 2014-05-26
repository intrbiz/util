package com.intrbiz.data.db.compiler.dialect.type;

import java.util.Set;

public class SQLEnumType implements SQLType
{    
    private final Class<? extends Enum<?>> type;

    public SQLEnumType(Class<? extends Enum<?>> type)
    {
        this.type = type;
    }

    @Override
    public String getSQLType()
    {
        return "TEXT";
    }

    @Override
    public Class<?>[] getJavaTypes()
    {
        return new Class<?>[] { this.type };
    }

    @Override
    public Class<?> getDefaultJavaType()
    {
        return this.type;
    }
    
    public boolean isCompatibleWith(Class<?> type)
    {
        return type.isAssignableFrom(this.type);
    }
    
    public void addImports(Set<String> imports)
    {
        imports.add(this.type.getCanonicalName());
    }
    
    public String setBinding(int idx, String value)
    {
        return "DBUtil.setEnum(stmt, " + idx + ", " + value + ")";
    }

    public String getBinding(int idx)
    {
        return "DBUtil.getEnum(rs, " + idx + ", " + this.type.getSimpleName() + ".class)";
    }
}
