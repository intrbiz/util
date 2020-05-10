package com.intrbiz.data.db.compiler.model;

import java.util.LinkedList;
import java.util.List;

public class Partitioning
{
    private List<Partition> partitions = new LinkedList<>();
    
    public Partitioning()
    {
        super();
    }

    public List<Partition> getPartitions()
    {
        return this.partitions;
    }

    public void setPartitions(List<Partition> partitions)
    {
        this.partitions = partitions;
    }
}
