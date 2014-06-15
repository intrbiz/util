package com.intrbiz.data.cache;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapConfig.EvictionPolicy;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.data.DataException;
import com.intrbiz.data.DataManager.CacheProvider;

public class HazelcastCacheProvider implements CacheProvider
{
    private Config hazelcastConfig;

    private HazelcastInstance hazelcastInstance;

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
                String hazelcastConfigFile = System.getProperty("hazelcast.config");
                if (hazelcastConfigFile != null)
                {
                    // when using a config file, you must configure the balsa.sessions map
                    this.hazelcastConfig = new XmlConfigBuilder(hazelcastConfigFile).build();
                }
                else
                {
                    // setup the default configuration
                    this.hazelcastConfig = new Config();
                    // add update configuration for our maps
                    MapConfig sessionMapConfig = this.hazelcastConfig.getMapConfig("intrbiz.cache.*");
                    // session lifetime is in minutes
                    sessionMapConfig.setMaxIdleSeconds(3600);
                    sessionMapConfig.setEvictionPolicy(EvictionPolicy.LRU);
                    sessionMapConfig.setEvictionPercentage(25);
                    this.hazelcastConfig.addMapConfig(sessionMapConfig);
                }
            }
            else
            {
                this.hazelcastConfig = config;
            }
            // create the hazel cast instance
            if (instanceName == null)
            {
                this.hazelcastInstance = Hazelcast.newHazelcastInstance(this.hazelcastConfig);
            }
            else
            {
                // set the instance name
                this.hazelcastConfig.setInstanceName(instanceName);
                // create the instance
                this.hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(this.hazelcastConfig);
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
        return new HazelcastCache(name, this.hazelcastInstance.getMap("intrbiz.cache." + name));
    }
}
