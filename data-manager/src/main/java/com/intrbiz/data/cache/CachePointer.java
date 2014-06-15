package com.intrbiz.data.cache;

import java.io.Serializable;

/**
 * A pointer to another cache entry
 */
public class CachePointer implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String key;
    
    public CachePointer()
    {
        super();
    }
    
    public CachePointer(String key)
    {
        super();
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }
    
    public String toString()
    {
        return "CachePointer(" + this.key + ")";
    }
}
