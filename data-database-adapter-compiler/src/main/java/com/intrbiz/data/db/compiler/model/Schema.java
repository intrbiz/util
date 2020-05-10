package com.intrbiz.data.db.compiler.model;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.intrbiz.data.db.DatabaseAdapter;

public class Schema
{
    private String name;

    private Version version;

    private List<Table> tables = new LinkedList<Table>();

    private List<Type> types = new LinkedList<Type>();

    private List<Function> functions = new LinkedList<Function>();

    private Class<?> definition;

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

    public Schema(String name, Class<?> definition)
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

    public Class<?> getDefinition()
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
    
    public SortedSet<Version> findAllPreviousVersions()
    {
        SortedSet<Version> versions = new TreeSet<>();
        for (Table table : this.tables)
        {
            versions.add(table.getSince());
            for (Column col : table.getColumns())
            {
                versions.add(col.getSince());
            }
            for (ForeignKey fk : table.getForeignKeys())
            {
                versions.add(fk.getSince());
            }
            for (Index idx : table.getIndexes())
            {
                versions.add(idx.getSince());
            }
        }
        for (Function func : this.getFunctions())
        {
            versions.add(func.getSince());
        }
        for (Type type : this.getTypes())
        {
            versions.add(type.getSince());
        }
        for (Patch patch : this.patches)
        {
            versions.add(patch.getVersion());
        }
        // remove the current version
        versions.remove(this.version);
        return versions;
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
