package com.intrbiz.data.db.compiler.model.function;

import com.intrbiz.data.db.compiler.model.Table;

public class SetterInfo
{
    private Table table;

    private boolean upsert = false;

    public SetterInfo()
    {
        super();
    }

    public Table getTable()
    {
        return table;
    }

    public void setTable(Table table)
    {
        this.table = table;
    }

    public boolean isUpsert()
    {
        return upsert;
    }

    public void setUpsert(boolean upsert)
    {
        this.upsert = upsert;
    }
}
