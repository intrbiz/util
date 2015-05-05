package com.intrbiz.data.cache;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Cache
{
    public enum CacheState { ON, OFF, WRITE_ONLY, READ_ONLY }
    
    /**
     * The name of this cache
     * @return
     */
    String name();
    
    /**
     * Get the value of the given key in the cache
     * @param key
     * @return
     */
    <T> T get(String key);
    
    /**
     * Get the key and follow the pointer
     * @param key
     * @return
     */
    default <T> T getAndFollow(String key)
    {
        CachePointer pointer = this.get(key);
        if (pointer == null) return null;
        return this.get(pointer.getKey());
    }
    
    default <T> List<T> getAndFollowList(String key)
    {
        CachePointerList pointer = this.get(key);
        if (pointer == null) return null;
        // process the pointer list
        // failing fast on a cache miss of a referenced key
        List<T> ret = new LinkedList<T>();
        for (String pointerKey : pointer.getKeys())
        {
            T element = this.get(pointerKey);
            if (element == null)
            {
                // we have a cache miss for an element 
                // in the list need to invalidate the list
                this.remove(key);
                return null;
            }
            ret.add(element);
        }
        return ret;
    }
    
    /**
     * Get the key and follow it if it is a pointer
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    default <T> T getOrFollow(String key)
    {
        Object value = this.get(key);
        if (value instanceof CachePointer)
        {
            // recursively follow
            return this.getOrFollow(((CachePointer) value).getKey());
        }
        return (T) value;
    }
    
    /**
     * Same as get
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    default <T> List<T> getList(String key)
    {
        return (List<T>) this.get(key);
    }
    
    @SuppressWarnings("unchecked")
    default <T> List<T> getOrFollowList(String key)
    {
        Object value = this.get(key);
        if (value instanceof CachePointerList)
        {
            // recursively follow
            // process the pointer list
            // failing fast on a cache miss of a referenced key
            List<T> ret = new LinkedList<T>();
            for (String pointerKey : ((CachePointerList) value).getKeys())
            {
                T element = this.getOrFollow(pointerKey);
                if (element == null)
                {
                    // we have a cache miss for an element 
                    // in the list need to invalidate the list
                    this.remove(key);
                    return null;
                }
                ret.add(element);
            }
            return ret;
        }
        return (List<T>) value;
    }
    
    /**
     * Put the given name value pair into the cache
     * A put will invalidate any keys which 
     * are dependent upon it.
     * @param key
     * @param value
     */
     <T> void put(String key, T value);
     
     /**
      * Add an entry to the cache which maps the given key to the given pointer (another key)
      * @param key
      * @param pointer
      */
     default void putPointer(String key, String pointer)
     {
         this.put(key, new CachePointer(pointer));
     }
     
     /**
      * Add an entry to the cache which maps the given key to the given list of pointers (a list of other keys)
      * @param ket
      * @param pointers
      */
     default void putPointerList(String key, List<String> pointers)
     {
         this.put(key, new CachePointerList(pointers));
     }
     
     /**
      * Same as putPointerList but using a lambda for the pointer
      * @param key
      * @param values
      * @param pointer
      */
     default <T> void putPointerList(String key, List<T> values, Function<T, String> pointer)
     {
         this.put(key, new CachePointerList(values.stream().map(pointer).collect(Collectors.toList())));
     }
     
     /**
      * Same as putPointer but using a lambda for the pointer
      * @param key
      * @param value
      * @param pointer
      */
     default <T> void putPointer(String key, T value, Function<T, String> pointer)
     {
         if (value != null)
         {
             this.put(key, new CachePointer(pointer.apply(value)));
         }
     }
     
     /**
      * Same as put but using a lambda for the key
      * @param value
      * @param key
      */
     default <T> void put(T value, Function<T, String> key)
     {
         if (value != null)
         {
             this.put(key.apply(value), value);
         }
     }
     
     /**
      * Same as put(T, Function<T, String>) for each value in the list
      * @param values
      * @param key
      */
     default <T> void put(List<T> values, Function<T, String> key)
     {
         for (T value : values)
         {
             this.put(key.apply(value), value);
         }
     }
    
    /**
     * Does this cache contain the given key
     * @param key
     * @return
     */
    boolean contains(String key);
    
    /**
     * Same as contains by using a lambda for the key
     * @param value
     * @param key
     * @return
     */
    default <T> boolean contains(T value, Function<T, String> key)
    {
        if (value != null)
        {
            return this.contains(key.apply(value));
        }
        return false;
    }
    
    /**
     * Remove the given key and any keys dependent on this key
     * @param key
     */
    void remove(String key);
    
    /**
     * Same as remove but using a lambda for the key
     * @param value
     * @param key
     */
    default <T> void remove(T value, Function<T, String> key)
    {
        if (value != null)
        {
            this.remove(key.apply(value));
        }
    }
    
    /**
     * Remove all keys which start with the given prefix
     * @param keyPrefix
     */
    default void removePrefix(String keyPrefix)
    {
        for (String key : this.keySet(keyPrefix))
        {
            this.remove(key);
        }
    }
    
    /**
     * Same as removePrefix but using a lambda for the key
     * @param value
     * @param keyPrefix
     */
    default <T> void removePrefix(T value, Function<T, String> keyPrefix)
    {
        if (value != null)
        {
            this.removePrefix(keyPrefix.apply(value));
        }
    }
    
    /**
     * Get all keys which start with the given prefix
     * @param keyPrefix
     * @return
     */
    Set<String> keySet(String keyPrefix);
    
    /**
     * Finished using this cache (Optional)
     */
    default void close()
    {
    }
    
    /**
     * Invalidate all cache entries (Optional)
     */
    void clear();
    
    /**
     * Listen for entries which are removed from the cache (Optional)
     * @param listener
     */
    default void addListener(CacheListener listener)
    {
    }
    
    /**
     * Remove the given listener
     * @param listener
     */
    default void removeListener(CacheListener listener)
    {
    }
    
    /**
     * Enable the cache
     */
    default void enable()
    {
        this.state(CacheState.ON);
    }
    
    /**
     * Disable the cache
     */
    default void disable()
    {
        this.state(CacheState.OFF);
    }
    
    /**
     * Make the cache read only
     */
    default void readOnly()
    {
        this.state(CacheState.READ_ONLY);
    }
    
    /**
     * Make the cache write only
     */
    default void writeOnly()
    {
        this.state(CacheState.WRITE_ONLY);
    }
    
    /**
     * Is this cache enabled
     */
    default CacheState state()
    {
        return CacheState.ON;
    }
    
    /**
     * Set the state of this cache state
     */
    default void state(CacheState state)
    {
    }
    
    /*
     * Transaction support 
     */
    
    /**
     * Does this cache instance support transactions
     */
    default boolean isTransactional()
    {
        return false;
    }
    
    /**
     * Begin a transaction against this cache
     */
    default void begin()
    {
    }
    
    /**
     * Commit the current changes made by the current transaction to the cache 
     */
    default void commit()
    {
    }
    
    /**
     * Rollback the current changes made by the current transaction to the cache 
     */
    default void rollback()
    {
    }
    
    /**
     * End the current transaction, going back to auto-commit mode, rolling back any uncommitted changes
     */
    default void end()
    {
        
    }
}
