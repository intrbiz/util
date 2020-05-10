package com.intrbiz.data.db.compiler.model;

import java.util.LinkedList;
import java.util.List;

public class Index
{
    private String name;
    
    private List<Column> columns = new LinkedList<Column>();
    
    private String using;
    
    private String expression;
    
    private Version since;
    
    public Index()
    {
        super();
    }
    
    public Index(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Column> getColumns()
    {
        return columns;
    }

    public void setColumns(List<Column> columns)
    {
        this.columns = columns;
    }
    
    public void addColumn(Column col)
    {
        this.columns.add(col);
    }
    
    public Column findColumn(String name)
    {
        for (Column col : this.getColumns())
        {
            if (name.equals(col.getName()))
                return col;
        }
        return null;
    }
    
    public String getUsing()
    {
        return this.using;
    }

    public void setUsing(String using)
    {
        this.using = using;
    }

    public String getExpression()
    {
        return this.expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public Version getSince()
    {
        return this.since;
    }

    public void setSince(Version since)
    {
        this.since = since;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Index ").append(this.getName()).append(" ").append(this.using).append("(");
        for (Column col : this.getColumns())
        {
            sb.append(col.getName()).append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
}
