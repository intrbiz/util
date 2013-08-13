package com.intrbiz.util.compiler.model;

public class JavaField
{
    private JavaClass javaClass;

    private String type;

    private String name;

    private String value;

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

    public JavaField setType(String type)
    {
        this.type = type;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public JavaField setName(String name)
    {
        this.name = name;
        return this;
    }

    public String getValue()
    {
        return value;
    }

    public JavaField setValue(String value)
    {
        this.value = value;
        return this;
    }

    public String toJava(String p)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(p).append("private ").append(this.getType()).append(" ").append(this.getName());
        if (this.getValue() != null)
        {
            sb.append(" = ").append(this.getValue());
        }
        sb.append(";\r\n");
        return sb.toString();
    }
}
