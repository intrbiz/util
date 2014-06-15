package com.intrbiz.data.cache;

public interface CacheListener
{
    /**
     * The given key has been removed from the cache
     * @param key the key which has been removed (or evicted)
     * @param entry the entry which was removed, note this maybe null
     */
    void onRemove(String key, Object entry);
    
    /**
     * The given key has been put 
     * @param key the key which has been updated
     * @param entry the entry which was updated, note this maybe null
     */
    void onPut(String key, Object entry);
}
