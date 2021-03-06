package com.intrbiz.data.db.compiler.dialect.type;

import java.util.Set;

public interface SQLType
{
    String getSQLType();
    
    Class<?> getDefaultJavaType();
    
    Class<?>[] getJavaTypes();
    
    boolean isCompatibleWith(Class<?> type);
    
    //
    
    void addImports(Set<String> imports);
    
    String setBinding(int idx, String value);
    
    String getBinding(int idx);
}
