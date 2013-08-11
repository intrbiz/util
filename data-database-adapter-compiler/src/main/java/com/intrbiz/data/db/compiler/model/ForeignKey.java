package com.intrbiz.data.db.compiler.model;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.Deferable;

public class ForeignKey
{
    private String name;

    private List<Column> columns = new LinkedList<Column>();

    private Table references;

    private List<Column> on = new LinkedList<Column>();

    private Action onUpdate;

    private Action onDelete;

    private Deferable deferable;

    public ForeignKey()
    {
        super();
    }

    public ForeignKey(String name)
    {
        super();
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Column> getColumns()
    {
        return columns;
    }

    public void setColumns(List<Column> columns)
    {
        this.columns = columns;
    }
    
    public void addColumn(Column col)
    {
        this.columns.add(col);
    }

    public Table getReferences()
    {
        return references;
    }

    public void setReferences(Table references)
    {
        this.references = references;
    }

    public List<Column> getOn()
    {
        return on;
    }

    public void setOn(List<Column> on)
    {
        this.on = on;
    }
    
    public void addOn(Column col)
    {
        this.on.add(col);
    }

    public Action getOnUpdate()
    {
        return onUpdate;
    }

    public void setOnUpdate(Action onUpdate)
    {
        this.onUpdate = onUpdate;
    }

    public Action getOnDelete()
    {
        return onDelete;
    }

    public void setOnDelete(Action onDelete)
    {
        this.onDelete = onDelete;
    }

    public Deferable getDeferable()
    {
        return deferable;
    }

    public void setDeferable(Deferable deferable)
    {
        this.deferable = deferable;
    }
}
