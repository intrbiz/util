package com.intrbiz.data.db.compiler.model.function;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.intrbiz.data.db.compiler.model.Order;
import com.intrbiz.data.db.compiler.model.Table;

public class GetterInfo
{
    private boolean parameterised = false;

    private Table table;

    private boolean offset = false;

    private boolean limit = false;

    private List<Order> orderBy = new LinkedList<Order>();
    
    private Map<String, String> query = new HashMap<String, String>();

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

    public Table getTable()
    {
        return table;
    }

    public void setTable(Table table)
    {
        this.table = table;
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
}
