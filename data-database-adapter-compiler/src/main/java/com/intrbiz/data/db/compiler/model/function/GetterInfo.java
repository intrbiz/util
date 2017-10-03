package com.intrbiz.data.db.compiler.model.function;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.intrbiz.data.db.compiler.model.Order;

public class GetterInfo implements UserDefinedInfo
{
    private boolean parameterised = false;

    private boolean offset = false;

    private boolean limit = false;

    private List<Order> orderBy = new LinkedList<Order>();
    
    private Map<String, String> query = new HashMap<String, String>();
    
    private Map<String, String[]> userDefined = new HashMap<String, String[]>();

    public GetterInfo()
    {
        super();
    }

    public boolean isOffset()
    {
        return offset;
    }

    public void setOffset(boolean offset)
    {
        this.offset = offset;
    }

    public boolean isLimit()
    {
        return limit;
    }

    public void setLimit(boolean limit)
    {
        this.limit = limit;
    }

    public boolean isParameterised()
    {
        return parameterised;
    }

    public void setParameterised(boolean parameterised)
    {
        this.parameterised = parameterised;
    }

    public List<Order> getOrderBy()
    {
        return orderBy;
    }

    public void setOrderBy(List<Order> orderBy)
    {
        this.orderBy = orderBy;
    }

    public void addOrderBy(Order order)
    {
        this.orderBy.add(order);
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
