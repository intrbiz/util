package com.intrbiz.util.compiler.model;

import java.util.LinkedList;
import java.util.List;

public class JavaMethod
{
    private JavaClass javaClass;

    private String returnType;

    private String name;

    private List<JavaParameter> parameters = new LinkedList<JavaParameter>();

    private StringBuilder code = new StringBuilder();

    public JavaMethod()
    {
        super();
    }

    public JavaMethod(JavaClass on, String name)
    {
        this();
        this.javaClass = on;
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

    public String getReturnType()
    {
        return returnType;
    }
    
    public boolean isVoid()
    {
        return this.getReturnType() == null;
    }

    public JavaMethod setReturnType(String returnType)
    {
        this.returnType = returnType;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public JavaMethod setName(String name)
    {
        this.name = name;
        return this;
    }

    public List<JavaParameter> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<JavaParameter> parameters)
    {
        this.parameters = parameters;
    }
    
    public JavaMethod addParameter(JavaParameter parameter)
    {
        this.parameters.add(parameter);
        return this;
    }

    public StringBuilder getCode()
    {
        return code;
    }

    public JavaMethod setCode(StringBuilder code)
    {
        this.code = code;
        return this;
    }
    
    /**
     * Append code to this method
     * @param code
     * @return
     */
    public JavaMethod append(String code)
    {
        this.code.append(code);
        return this;
    }
    
    public String toJava(String p)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(p).append("public ").append(this.getReturnType() == null ? "void" : this.getReturnType()).append(" ").append(this.getName()).append("(");
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
