package com.intrbiz.data.db.compiler.dialect.type;

import java.util.Set;

import com.intrbiz.data.db.compiler.model.Type;

public class SQLCompositeType implements SQLType
{
    private final Type type;

    private final Class<?> javaType;

    public SQLCompositeType(Type type, Class<?> javaType)
    {
        this.type = type;
        this.javaType = javaType;
    }

    @Override
    public String getSQLType()
    {
        return "\"" + type.getSchema().getName() + "\".\"" + type.getName() + "\"";
    }

    @Override
    public Class<?> getDefaultJavaType()
    {
        return this.javaType;
    }

    @Override
    public Class<?>[] getJavaTypes()
    {
        return new Class<?>[] { this.javaType };
    }
    
    public boolean isCompatibleWith(Class<?> type)
    {
        for (Class<?> c : this.getJavaTypes())
        {
            if (type.isAssignableFrom(c))
                return true;
        }
        return false;
    }
    
    public void addImports(Set<String> imports)
    {
    }
    
    public String setBinding(int idx, String value)
    {
        throw new RuntimeException("Cannot bind a composite type");
    }
    
    public String getBinding(int idx)
    {
        throw new RuntimeException("Cannot bind a composite type");
    }
}
