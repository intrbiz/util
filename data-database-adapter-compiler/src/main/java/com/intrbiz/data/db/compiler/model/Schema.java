package com.intrbiz.data.db.compiler.model;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.data.db.DatabaseAdapter;

public class Schema
{
    private String name;

    private Version version;

    private List<Table> tables = new LinkedList<Table>();

    private List<Type> types = new LinkedList<Type>();

    private List<Function> functions = new LinkedList<Function>();

    private Class<? extends DatabaseAdapter> definition;

    private List<Patch> patches = new LinkedList<Patch>();

    public Schema()
    {
        super();
    }

    public Schema(String name)
    {
        super();
        this.name = name;
    }

    public Schema(String name, Class<? extends DatabaseAdapter> definition)
    {
        super();
        this.name = name;
        this.definition = definition;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Version getVersion()
    {
        return version;
    }

    public void setVersion(Version version)
    {
        this.version = version;
    }

    public List<Table> getTables()
    {
        return tables;
    }

    public void setTables(List<Table> tables)
    {
        this.tables = tables;
    }

    public void addTable(Table table)
    {
        this.tables.add(table);
        table.setSchema(this);
    }

    public List<Type> getTypes()
    {
        return types;
    }

    public void setTypes(List<Type> types)
    {
        this.types = types;
    }

    public void addType(Type type)
    {
        this.types.add(type);
        type.setSchema(this);
    }

    public List<Function> getFunctions()
    {
        return functions;
    }

    public void setFunctions(List<Function> functions)
    {
        this.functions = functions;
    }

    public void addFunction(Function function)
    {
        this.functions.add(function);
        function.setSchema(this);
    }

    public Class<? extends DatabaseAdapter> getDefinition()
    {
        return definition;
    }

    public void setDefinition(Class<? extends DatabaseAdapter> definition)
    {
        this.definition = definition;
    }

    public List<Patch> getPatches()
    {
        return patches;
    }

    public void setPatches(List<Patch> patches)
    {
        this.patches = patches;
    }
    
    public void addPatch(Patch patch)
    {
        this.patches.add(patch);
    }
    
    public void addPatches(List<Patch> patches)
    {
        this.patches.addAll(patches);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("SCHEMA ").append(this.getName()).append(";\r\n\r\n");
        for (Table t : this.getTables())
        {
            sb.append(t.toString()).append("\r\n");
        }
        for (Type t : this.getTypes())
        {
            sb.append(t.toString()).append("\r\n");
        }
        return sb.toString();
    }
}
