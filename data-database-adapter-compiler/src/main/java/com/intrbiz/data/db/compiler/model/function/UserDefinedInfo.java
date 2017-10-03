package com.intrbiz.data.db.compiler.model.function;

import java.util.Map;

public interface UserDefinedInfo
{
    Map<String, String[]> getUserDefined();

    void setUserDefined(Map<String, String[]> userDefined);
    
    void addUserDefined(String dialect, String[] sql);
    
    String[] getUserDefined(String dialect);
    
    boolean hasUserDefined();
}
