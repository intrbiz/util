package com.intrbiz.data.db.compiler.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
    
    private Partitioning partitioning;
    
    private List<Index> indexes = new LinkedList<Index>();

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
    
    public List<Column> findColumnsSince(Version version)
    {
        return this.columns.stream().filter((c) -> { return c.getSince().isAfter(version);  }).collect(Collectors.toList());
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
            if (this.getPrimaryKey() != null && this.getPrimaryKey().findColumn(col.getName()) == null)
                cols.add(col);
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
    
    public List<ForeignKey> findForeignKeysSince(Version version)
    {
        return this.foreignKeys.stream().filter((c) -> { return c.getSince().isAfter(version);  }).collect(Collectors.toList());
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
    
    public Partitioning getPartitioning()
    {
        return this.partitioning;
    }

    public void setPartitioning(Partitioning partitioning)
    {
        this.partitioning = partitioning;
    }

    public List<Index> getIndexes()
    {
        return this.indexes;
    }
    
    public List<Index> findIndexesSince(Version version)
    {
        return this.indexes.stream().filter((c) -> { return c.getSince().isAfter(version);  }).collect(Collectors.toList());
    }

    public void setIndexes(List<Index> indexes)
    {
        this.indexes = indexes;
    }
    
    public void addIndex(Index index)
    {
        this.indexes.add(index);
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
        sb.append("TABLE ").append(this.getName()).append(" (\r\n");
        for (Column col : this.getColumns())
        {
            sb.append("    ").append(col.toString()).append("\r\n");
        }
        sb.append(");\r\n");
        return sb.toString();
    }
}
