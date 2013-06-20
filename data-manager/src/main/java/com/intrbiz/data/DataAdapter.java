package com.intrbiz.data;



/**
 * Generic adapter to some data source
 */
public interface DataAdapter extends AutoCloseable
{
    /**
     * Get the name of the adapter
     * @return
     */
    String getName();
    
    /**
     * Close the adapter denoting the user has no further 
     * use for the adapter, so the adapter can cleanup any 
     * resources used.
     */
    void close();
}
