package com.intrbiz.data.db.model;

import com.intrbiz.data.db.DatabaseAdapter;

public class DTO<T extends DatabaseAdapter>
{
    private transient T adapter;
    
    public DTO()
    {
        super();
    }
    
    public DTO(T adapter)
    {
        this();
        this.adapter = adapter;
    }
    
    public final T getAdapter()
    {
        return this.adapter;
    }
    
    public final void setAdapter(T adapter)
    {
        this.adapter = adapter;
    }
}
