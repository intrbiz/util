package com.intrbiz.data.db.compiler.model;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.data.db.compiler.meta.PartitionMode;

public class Partition
{
    private PartitionMode mode;
    
    private List<Column> on = new LinkedList<>();
    
    private boolean indexOn;
    
    private String indexOnUsing;
    
    public Partition()
    {
        super();
    }

    public PartitionMode getMode()
    {
        return this.mode;
    }

    public void setMode(PartitionMode mode)
    {
        this.mode = mode;
    }

    public List<Column> getOn()
    {
        return this.on;
    }

    public void setOn(List<Column> on)
    {
        this.on = on;
    }

    public boolean isIndexOn()
    {
        return this.indexOn;
    }

    public void setIndexOn(boolean indexOn)
    {
        this.indexOn = indexOn;
    }

    public String getIndexOnUsing()
    {
        return this.indexOnUsing;
    }

    public void setIndexOnUsing(String indexOnUsing)
    {
        this.indexOnUsing = indexOnUsing;
    }
}
