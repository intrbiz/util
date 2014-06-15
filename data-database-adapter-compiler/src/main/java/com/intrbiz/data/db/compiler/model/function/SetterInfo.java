package com.intrbiz.data.db.compiler.model.function;


public class SetterInfo
{
    private boolean upsert = false;

    public SetterInfo()
    {
        super();
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
