package com.intrbiz.util.compiler.model;

public class JavaField
{
    private JavaClass javaClass;

    private String type;

    private String name;

    public JavaField()
    {
        super();
    }

    public JavaField(JavaClass on)
    {
        this();
        this.javaClass = on;
    }

    public JavaField(JavaClass on, String type, String name)
    {
        this(on);
        this.type = type;
        this.name = name;
    }

    public JavaClass getJavaClass()
    {
        return javaClass;
    }

    public void setJavaClass(JavaClass javaClass)
    {
        this.javaClass = javaClass;
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
        return p + "private " + this.getType() + " " + this.getName() + ";\r\n";
    }
}
