package com.intrbiz.util.compiler.model;

import java.util.LinkedList;
import java.util.List;

public class JavaConstructor
{
    private JavaClass javaClass;

    private List<JavaParameter> parameters = new LinkedList<JavaParameter>();

    private StringBuilder code = new StringBuilder();

    public JavaConstructor()
    {
        super();
    }

    public JavaConstructor(JavaClass on)
    {
        this();
        this.javaClass = on;
    }

    public JavaClass getJavaClass()
    {
        return javaClass;
    }

    public void setJavaClass(JavaClass javaClass)
    {
        this.javaClass = javaClass;
    }

    public List<JavaParameter> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<JavaParameter> parameters)
    {
        this.parameters = parameters;
    }

    public JavaConstructor addParameter(JavaParameter parameter)
    {
        this.parameters.add(parameter);
        return this;
    }

    public StringBuilder getCode()
    {
        return code;
    }

    public void setCode(StringBuilder code)
    {
        this.code = code;
    }
    
    /**
     * Append code to this constructor
     * @param code
     * @return
     */
    public JavaConstructor append(String code)
    {
        this.code.append(code);
        return this;
    }
    
    public String toJava(String p)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(p).append("public ").append(this.getJavaClass().getName()).append("(");
        boolean ns = false;
        for (JavaParameter a : this.getParameters())
        {
            if (ns) sb.append(", ");
            sb.append(a.toJava(p));
            ns = true;
        }
        sb.append(")\r\n");
        sb.append(p).append("{\r\n");
        for (String s : this.getCode().toString().split("\n|\r\n|\r"))
        {
            if (s != null) sb.append(p + "\t").append(s).append("\r\n");
        }
        sb.append(p).append("}\r\n");
        return sb.toString();
    }
}
