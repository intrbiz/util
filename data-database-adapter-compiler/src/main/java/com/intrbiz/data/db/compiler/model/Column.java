package com.intrbiz.data.db.compiler.model;

import java.lang.reflect.Field;

import com.intrbiz.data.db.compiler.dialect.type.SQLType;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class Column implements Comparable<Column>
{
    private int index = 1;

    private String name;

    private SQLType type;

    private Field definition;

    private Version since;

    private boolean notNull;

    private Class<? extends DBTypeAdapter<?, ?>> adapter;

    public Column()
    {
        super();
    }

    public Column(String name, SQLType type)
    {
        this();
        this.name = name;
        this.type = type;
    }

    public Column(int index, String name, SQLType type)
    {
        this(name, type);
        this.index = index;
    }

    public Column(int index, String name, SQLType type, Field definition, boolean notNull, Class<? extends DBTypeAdapter<?, ?>> adapter)
    {
        this(name, type);
        this.index = index;
        this.definition = definition;
        this.notNull = notNull;
        this.adapter = adapter;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public SQLType getType()
    {
        return type;
    }

    public void setType(SQLType type)
    {
        this.type = type;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public Field getDefinition()
    {
        return definition;
    }

    public void setDefinition(Field definition)
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

    public boolean isNotNull()
    {
        return notNull;
    }

    public void setNotNull(boolean notNull)
    {
        this.notNull = notNull;
    }

    public Class<? extends DBTypeAdapter<?, ?>> getAdapter()
    {
        return adapter;
    }

    public void setAdapter(Class<? extends DBTypeAdapter<?, ?>> adapter)
    {
        this.adapter = adapter;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Column other = (Column) obj;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        return true;
    }

    @Override
    public int compareTo(Column o)
    {
        return this.index - o.index;
    }

    public String toString()
    {
        return this.getName() + " " + this.getType().getSQLType() + "[" + this.getType().getDefaultJavaType().getSimpleName() + "]";
    }
}
