package com.intrbiz.data.db.compiler.model.function;

import java.util.HashMap;
import java.util.Map;


public class RemoveInfo implements UserDefinedInfo
{
    private Map<String, String> query = new HashMap<String, String>();
    
    private Map<String, String[]> userDefined = new HashMap<String, String[]>();
    
    public RemoveInfo()
    {
        super();
    }
    
    public Map<String, String> getQuery()
    {
        return query;
    }

    public void setQuery(Map<String, String> query)
    {
        this.query = query;
    }
    
    public void addQuery(String dialect, String query)
    {
        this.query.put(dialect, query);
    }
    
    public String getQuery(String dialect)
    {
        String query = this.query.get(dialect);
        if (query == null) query = this.query.get("SQL");
        return query;
    }
    
    public boolean hasQuery()
    {
        return ! this.query.isEmpty();
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
