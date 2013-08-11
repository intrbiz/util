package com.intrbiz.data.db.compiler.dialect.type;

public interface SQLType
{
    String getSQLType();
    
    Class<?> getDefaultJavaType();
    
    Class<?>[] getJavaTypes();
    
    //
    
    String setBinding(String padding, int idx, String value);
    
    String getBinding(int idx);
}
