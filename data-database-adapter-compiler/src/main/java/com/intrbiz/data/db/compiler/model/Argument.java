package com.intrbiz.data.db.compiler.model;

import com.intrbiz.data.db.compiler.dialect.type.SQLType;

public class Argument
{
    private int index = 1;

    private String name;

    private SQLType type;

    private Class<?> javaClass;

    private Column shadowOf;

    private boolean optional = false;

    public Argument()
    {
        super();
    }

    public Argument(String name, SQLType type)
    {
        this();
        this.name = name;
        this.type = type;
    }

    public Argument(int index, String name, SQLType type)
    {
        this(name, type);
        this.index = index;
    }

    public Argument(int index, String name, SQLType type, Class<?> javaClass)
    {
        this(name, type);
        this.index = index;
        this.javaClass = javaClass;
    }

    public Argument(int index, String name, SQLType type, Class<?> javaClass, Column shadowOf)
    {
        this(name, type);
        this.index = index;
        this.javaClass = javaClass;
        this.shadowOf = shadowOf;
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

    public Class<?> getJavaClass()
    {
        return javaClass;
    }

    public void setJavaClass(Class<?> javaClass)
    {
        this.javaClass = javaClass;
    }

    public Column getShadowOf()
    {
        return shadowOf;
    }

    public void setShadowOf(Column shadowOf)
    {
        this.shadowOf = shadowOf;
    }

    public boolean isOptional()
    {
        return optional;
    }

    public void setOptional(boolean optional)
    {
        this.optional = optional;
    }
}
