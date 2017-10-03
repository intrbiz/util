package com.intrbiz.data.db.compiler.model.function;

import java.util.HashMap;
import java.util.Map;

public class SetterInfo implements UserDefinedInfo
{
    private boolean upsert = false;
    
    private Map<String, String[]> userDefined = new HashMap<String, String[]>();

    public SetterInfo()
    {
        super();
    }

    public boolean isUpsert()
    {
        return upsert;
    }

    public void setUpsert(boolean upsert)
    {
        this.upsert = upsert;
    }
    
    public Map<String, String[]> getUserDefined()
    {
        return userDefined;
    }

    public void setUserDefined(Map<String, String[]> userDefined)
    {
        this.userDefined = userDefined;
    }
    
    public void addUserDefined(String dialect, String[] sql)
    {
        this.userDefined.put(dialect, sql);
    }
    
    public String[] getUserDefined(String dialect)
    {
        String[] sql = this.userDefined.get(dialect);
        if (sql == null) sql = this.userDefined.get("SQL");
        return sql;
    }
    
    public boolean hasUserDefined()
    {
        return ! this.userDefined.isEmpty();
    }
}
