package com.intrbiz.data.db.compiler.dialect.type;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.intrbiz.data.db.util.DBUtil;

public class SQLArrayType implements SQLType
{
    private final String sqlType;
    
    private final String elementType;
    
    private final Class<?>[] javaTypes;
    
    private final Class<?> javaElementType;

    public SQLArrayType(String sqlType, String elementType, Class<?> javaElementType)
    {
        this.sqlType = sqlType;
        this.elementType = elementType;
        this.javaTypes = new Class<?>[] { Collection.class, List.class};
        this.javaElementType = javaElementType;
    }

    @Override
    public String getSQLType()
    {
        return this.sqlType;
    }
    
    public String getElementType()
    {
        return this.elementType;
    }
    
    public Class<?> getJavaElementType()
    {
        return this.javaElementType;
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
        imports.add(DBUtil.class.getCanonicalName());
        imports.add(this.javaElementType.getCanonicalName());
    }
    
    public String setBinding(int idx, String value)
    {
        return "DBUtil.setArray(stmt, " + idx + ", \"" + this.elementType + "\", " + value + ")";
    }
    
    public String getBinding(int idx)
    {
        return "DBUtil.getArray(rs, " + idx + ")";
    }
}
