package com.intrbiz.data.cache;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.Util;
import com.intrbiz.data.DataException;
import com.intrbiz.data.DataManager.CacheProvider;

public class HazelcastCacheProvider implements CacheProvider
{
    private HazelcastInstance hazelcastInstance;
    
    public static final String MAP_PREFIX = "intrbiz.cache.";

    public HazelcastCacheProvider(HazelcastInstance hazelcastInstance)
    {
        this.hazelcastInstance = hazelcastInstance;
    }

    public HazelcastCacheProvider(String instanceName)
    {
        this(null, instanceName);
    }
    
    public HazelcastCacheProvider()
    {
        this(null, null);
    }

    public HazelcastCacheProvider(Config config, String instanceName)
    {
        try
        {
            if (config == null)
            {
                // setup config
                String hazelcastConfigFile = Util.coalesceEmpty(System.getProperty("hazelcast.config"), System.getenv("hazelcast.config"));
                if (hazelcastConfigFile != null)
                {
                    // when using a config file, you should configure the intrbiz.cache. maps
                    config = new XmlConfigBuilder(hazelcastConfigFile).build();
                }
                else
                {
                    // setup the default configuration
                    config = new Config();
                    // add update configuration for our maps
                    MapConfig cacheMapConfig = config.getMapConfig(HazelcastCacheProvider.MAP_PREFIX + "*");
                    // add default config for cache maps
                    cacheMapConfig.setMaxIdleSeconds(1 * 60 * 60); /* Objects are removed if they are idle for 1 hour */
                    cacheMapConfig.setEvictionPolicy(EvictionPolicy.LRU);
                    cacheMapConfig.setTimeToLiveSeconds(12 * 60 * 60); /* Objects are always refreshed every 12 hours */
                    cacheMapConfig.setBackupCount(0); /* We're a cache we don't care if we need to visit the backing store */
                    cacheMapConfig.setAsyncBackupCount(0); /* We're a cache we don't care if we need to visit the backing store */
                    cacheMapConfig.setInMemoryFormat(InMemoryFormat.OBJECT); /* Store the objects in object form, we want retrival to be fast and consitent */
                    // setup nearline cache
                    NearCacheConfig cacheMapNLConfig = new NearCacheConfig();
                    cacheMapNLConfig.setCacheLocalEntries(false);
                    cacheMapNLConfig.setEvictionPolicy("LRU");
                    cacheMapNLConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
                    cacheMapNLConfig.setInvalidateOnChange(true);
                    cacheMapNLConfig.setMaxIdleSeconds(10 * 60); /* 10 minute idle time */
                    cacheMapNLConfig.setTimeToLiveSeconds(1 * 60 * 60); /* 1 hour TTL */
                    cacheMapConfig.setNearCacheConfig(cacheMapNLConfig);
                    config.addMapConfig(cacheMapConfig);
                }
            }
            // create the hazel cast instance
            if (instanceName == null)
            {
                this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);
            }
            else
            {
                // set the instance name
                config.setInstanceName(instanceName);
                // create the instance
                this.hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(config);
            }
        }
        catch (Exception e)
        {
            throw new DataException("Failed to start Hazelcast Cache Provider", e);
        }
    }

    public HazelcastInstance getHazelcastInstance()
    {
        return hazelcastInstance;
    }

    @Override
    public Cache getCache(String name)
    {
        return new HazelcastCache(name, this.hazelcastInstance);
    }
}
