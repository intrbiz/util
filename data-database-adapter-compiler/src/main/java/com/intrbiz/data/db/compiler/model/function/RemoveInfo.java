package com.intrbiz.data.db.compiler.model.function;

import java.util.HashMap;
import java.util.Map;


public class RemoveInfo
{
    private Map<String, String> query = new HashMap<String, String>();
    
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
}
