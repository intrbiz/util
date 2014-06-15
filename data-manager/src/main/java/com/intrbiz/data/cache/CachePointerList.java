package com.intrbiz.data.cache;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * A pointer to a list of other cache entries
 */
public class CachePointerList implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private List<String> keys;
    
    public CachePointerList()
    {
        super();
        this.keys = new LinkedList<String>();
    }
    
    public CachePointerList(List<String> keys)
    {
        super();
        this.keys = keys;
    }

    public List<String> getKeys()
    {
        return keys;
    }

    public void setKeys(List<String> keys)
    {
        this.keys = keys;
    }
    
    public String toString()
    {
        return "CachePointerList(" + this.keys + ")";
    }
}
