package com.intrbiz.data.db.compiler.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.data.db.compiler.dialect.type.SQLType;

public class Function
{
    private Schema schema;

    private String name;

    private boolean returnsList;

    private SQLType returnType;

    private List<Argument> arguments = new LinkedList<Argument>();

    private Method definition;

    private Annotation functionType;

    private Object introspectionInformation;

    private Version since;

    private boolean cacheable = false;

    private List<String> cacheInvalidate = new LinkedList<String>();

    private Table table;

    public Function()
    {
        super();
    }

    public Function(String name)
    {
        super();
        this.name = name;
    }

    public Function(Schema schema, String name)
    {
        super();
        this.schema = schema;
        this.name = name;
    }

    public Function(Schema schema, String name, Method definition, Annotation functionType)
    {
        super();
        this.schema = schema;
        this.name = name;
        this.definition = definition;
        this.functionType = functionType;
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

    public boolean isReturnsList()
    {
        return returnsList;
    }

    public void setReturnsList(boolean returnsList)
    {
        this.returnsList = returnsList;
    }

    public SQLType getReturnType()
    {
        return returnType;
    }

    public void setReturnType(SQLType returnType)
    {
        this.returnType = returnType;
    }

    public List<Argument> getArguments()
    {
        return arguments;
    }

    public void setArguments(List<Argument> arguments)
    {
        this.arguments = arguments;
    }

    public void addArgument(Argument argument)
    {
        this.arguments.add(argument);
    }

    public Argument findArgument(String name)
    {
        for (Argument arg : this.getArguments())
        {
            if (name.equals(arg.getName())) return arg;
        }
        return null;
    }

    public Method getDefinition()
    {
        return definition;
    }

    public void setDefinition(Method definition)
    {
        this.definition = definition;
    }

    public Annotation getFunctionType()
    {
        return functionType;
    }

    public void setFunctionType(Annotation functionType)
    {
        this.functionType = functionType;
    }

    public Object getIntrospectionInformation()
    {
        return introspectionInformation;
    }

    public void setIntrospectionInformation(Object introspectionInformation)
    {
        this.introspectionInformation = introspectionInformation;
    }

    public Version getSince()
    {
        return since;
    }

    public void setSince(Version since)
    {
        this.since = since;
    }

    public String getSignature()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getSchema().getName()).append(".").append(this.getName()).append("(");
        boolean ns = false;
        for (Argument arg : this.getArguments())
        {
            if (ns) sb.append(",");
            sb.append(arg.getType().getSQLType());
            ns = true;
        }
        sb.append(")");
        return sb.toString();
    }

    public boolean isCacheable()
    {
        return cacheable;
    }

    public void setCacheable(boolean cacheable)
    {
        this.cacheable = cacheable;
    }

    public List<String> getCacheInvalidate()
    {
        return cacheInvalidate;
    }

    public void setCacheInvalidate(List<String> cacheInvalidate)
    {
        this.cacheInvalidate = cacheInvalidate;
    }

    public Table getTable()
    {
        return table;
    }

    public void setTable(Table table)
    {
        this.table = table;
    }

    public boolean isAllArgumentsPrimaryKey()
    {
        if (this.getTable() == null) return false;
        if (this.getTable().getPrimaryKey() == null) return false;
        if (this.getTable().getPrimaryKey().getColumns().size() != this.getArguments().size()) return false;
        for (Argument argument : this.getArguments())
        {
            Column shadow = argument.getShadowOf();
            if (shadow == null) return false;
            if (!this.getTable().getPrimaryKey().getColumns().stream().anyMatch((e) -> {
                return shadow.equals(e);
            })) return false;
        }
        return true;
    }
}
