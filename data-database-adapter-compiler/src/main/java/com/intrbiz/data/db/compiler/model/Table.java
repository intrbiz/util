package com.intrbiz.data.db.compiler.model;

import java.util.LinkedList;
import java.util.List;

public class Table
{
    private Schema schema;

    private String name;

    private List<Column> columns = new LinkedList<Column>();

    private PrimaryKey primaryKey;

    private List<ForeignKey> foreignKeys = new LinkedList<ForeignKey>();

    private Class<?> definition;

    private Version since;

    private List<Unique> uniques = new LinkedList<Unique>();

    private boolean virtual = false;

    public Table()
    {
        super();
    }

    public Table(String name)
    {
        this();
        this.name = name;
    }

    public Table(Schema schema, String name)
    {
        this();
        this.schema = schema;
        this.name = name;
    }

    public Table(Schema schema, String name, Class<?> definition, boolean virtual)
    {
        this();
        this.schema = schema;
        this.name = name;
        this.definition = definition;
        this.virtual = virtual;
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

    public PrimaryKey getPrimaryKey()
    {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey primaryKey)
    {
        this.primaryKey = primaryKey;
    }

    public List<Column> getNonPrimaryColumns()
    {
        List<Column> cols = new LinkedList<Column>();
        for (Column col : this.getColumns())
        {
            if (this.getPrimaryKey().findColumn(col.getName()) == null) cols.add(col);
        }
        return cols;
    }

    public List<ForeignKey> getForeignKeys()
    {
        return foreignKeys;
    }

    public void setForeignKeys(List<ForeignKey> foreignKeys)
    {
        this.foreignKeys = foreignKeys;
    }

    public void addForeignKey(ForeignKey key)
    {
        this.foreignKeys.add(key);
    }

    public Class<?> getDefinition()
    {
        return definition;
    }

    public void setDefinition(Class<?> definition)
    {
        this.definition = definition;
    }

    public Version getSince()
    {
        return since;
    }

    public void setSince(Version since)
    {
        this.since = since;
    }

    public List<Unique> getUniques()
    {
        return uniques;
    }

    public void setUniques(List<Unique> uniques)
    {
        this.uniques = uniques;
    }

    public void addUnique(Unique unq)
    {
        this.uniques.add(unq);
    }

    public boolean isVirtual()
    {
        return virtual;
    }

    public void setVirtual(boolean virtual)
    {
        this.virtual = virtual;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("TABLE ").append(this.getName()).append(" (\r\n");
        for (Column col : this.getColumns())
        {
            sb.append("    ").append(col.toString()).append("\r\n");
        }
        sb.append(");\r\n");
        return sb.toString();
    }
}
