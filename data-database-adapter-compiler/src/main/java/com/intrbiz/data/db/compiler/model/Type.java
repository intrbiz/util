package com.intrbiz.data.db.compiler.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Type
{
    private Schema schema;

    private String name;

    private List<Column> columns = new LinkedList<Column>();

    private Version since;

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
    
    public Column findColumn(String name)
    {
        for (Column col : this.getColumns())
        {
            if (name.equals(col.getName())) return col;
        }
        return null;
    }
    
    public List<Column> findColumnsSince(Version version)
    {
        return this.columns.stream().filter((c) -> { return c.getSince().isAfter(version);  }).collect(Collectors.toList());
    }

    public Version getSince()
    {
        return since;
    }

    public void setSince(Version since)
    {
        this.since = since;
    }
    
    /**
     * Perform final operations on the table, 
     * such as sorting the columns
     */
    public void finish()
    {
        // sort the columns
        Collections.sort(this.columns);
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
