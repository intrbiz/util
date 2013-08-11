package com.intrbiz.util.compiler.model;

public class JavaParameter
{
    private String type;

    private String name;

    public JavaParameter()
    {
        super();
    }

    public JavaParameter(String type, String name)
    {
        this();
        this.type = type;
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public String toJava(String p)
    {
        return "final " + this.getType() + " " + this.getName();
    }
}
