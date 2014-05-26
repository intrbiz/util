package com.intrbiz.data.db.compiler.model;

import java.util.LinkedList;
import java.util.List;

public class Unique
{
    private String name;
    
    private List<Column> columns = new LinkedList<Column>();
    
    public Unique()
    {
        super();
    }
    
    public Unique(String name)
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
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("UNIQUE ").append(this.getName()).append(" (");
        for (Column col : this.getColumns())
        {
            sb.append(col.getName()).append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
}
