package com.intrbiz.data.cache.hazelcast;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.intrbiz.data.DataManager.CacheProvider;
import com.intrbiz.data.cache.Cache;
import com.intrbiz.data.cache.HazelcastCacheProvider;

public class HazelcastCacheTest
{
    private static CacheProvider provider;
    
    private Cache cache;
    
    @BeforeClass
    public static void setup()
    {
        provider = new HazelcastCacheProvider();
    }
    
    @Before
    public void setupCache()
    {
        this.cache = provider.getCache("test-" + UUID.randomUUID());
    }
    
    @Test
    public void testSimplePut()
    {
        this.cache.put("key_1", "value_1");
        this.cache.put("key_2", "value_2");
        this.cache.put("key_3", "value_3");
        assertThat(this.cache.get("key_1"), is(equalTo("value_1")));
        assertThat(this.cache.get("key_2"), is(equalTo("value_2")));
        assertThat(this.cache.get("key_3"), is(equalTo("value_3")));
    }
    
    @Test
    public void testSimpleRemove()
    {
        this.cache.put("key_1", "value_1");
        assertThat(this.cache.get("key_1"), is(equalTo("value_1")));
        this.cache.remove("key_1");
        assertThat(this.cache.get("key_1"), is(nullValue()));
    }
    
    @Test
    public void testKeyset()
    {
        this.cache.put("key_1", "value_1");
        this.cache.put("key_2", "value_2");
        this.cache.put("key_3", "value_3");
        this.cache.put("key1", "value1");
        this.cache.put("key2", "value2");
        this.cache.put("key3", "value3");
        Set<String> keySet = this.cache.keySet("key_");
        assertThat(keySet.contains("key_1"), is(equalTo(true)));
        assertThat(keySet.contains("key_2"), is(equalTo(true)));
        assertThat(keySet.contains("key_3"), is(equalTo(true)));
        assertThat(keySet.contains("key1"), is(equalTo(false)));
        assertThat(keySet.contains("key2"), is(equalTo(false)));
        assertThat(keySet.contains("key3"), is(equalTo(false)));
    }
    
    @Test
    public void testTransactionPut()
    {
        this.cache.begin();
        try
        {
            this.cache.put("key_1", "value_1");
            this.cache.put("key_2", "value_2");
            this.cache.put("key_3", "value_3");
            assertThat(this.cache.get("key_1"), is(equalTo("value_1")));
            assertThat(this.cache.get("key_2"), is(equalTo("value_2")));
            assertThat(this.cache.get("key_3"), is(equalTo("value_3")));
            this.cache.commit();
        }
        finally
        {
            this.cache.end();
        }
        assertThat(this.cache.get("key_1"), is(equalTo("value_1")));
        assertThat(this.cache.get("key_2"), is(equalTo("value_2")));
        assertThat(this.cache.get("key_3"), is(equalTo("value_3")));
    }
    
    @Test
    public void testTransactionRemove()
    {
        this.cache.begin();
        try
        {
            this.cache.put("key_1", "value_1");
            assertThat(this.cache.get("key_1"), is(equalTo("value_1")));
            this.cache.remove("key_1");
            assertThat(this.cache.get("key_1"), is(nullValue()));
            this.cache.commit();
        }
        finally
        {
            this.cache.end();
        }
        assertThat(this.cache.get("key_1"), is(nullValue()));
    }
}
