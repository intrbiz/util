package com.intrbiz.data.db.compiler.model;

import java.util.LinkedList;
import java.util.List;

public class Type
{
    private Schema schema;

    private String name;

    private List<Column> columns = new LinkedList<Column>();

    public Type()
    {
        super();
    }

    public Type(String name)
    {
        this();
        this.name = name;
    }

    public Type(Schema schema, String name)
    {
        this();
        this.schema = schema;
        this.name = name;
    }

    public Schema getSchema()
    {
        return schema;
    }

    public void setSchema(Schema schema)
    {
        this.schema = schema;
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

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Type ").append(this.getName()).append(" (\r\n");
        for (Column col : this.getColumns())
        {
            sb.append("    ").append(col.toString()).append(",\r\n");
        }
        sb.append(");\r\n");
        return sb.toString();
    }
}
