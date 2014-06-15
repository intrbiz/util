package com.intrbiz.data.cache;

import java.util.Map.Entry;

import com.hazelcast.query.Predicate;

public class KeyPrefixPredicate implements Predicate<String, Object>
{
    private static final long serialVersionUID = 1L;
    
    private String prefix;
    
    public KeyPrefixPredicate()
    {
        super();
    }
    
    public KeyPrefixPredicate(String prefix)
    {
        this();
        this.prefix = prefix;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    @Override
    public boolean apply(Entry<String, Object> mapEntry)
    {
        return mapEntry.getKey().startsWith(this.prefix);
    }
}
