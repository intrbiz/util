import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.data.cache.Cache;
import com.intrbiz.data.cache.HazelcastCacheProvider;
import com.intrbiz.data.cache.tiered.TieredCacheProvider;


public class DistributedTieredCacheTest
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        //
        TieredCacheProvider tp1 = new TieredCacheProvider(new HazelcastCacheProvider());
        TieredCacheProvider tp2 = new TieredCacheProvider(new HazelcastCacheProvider());
        //
        Thread.sleep(5_000);
        //
        Cache t1 = tp1.getCache("test");
        Cache t2 = tp2.getCache("test");
        //
        System.out.println("Put T1");
        t1.put("key1", "value1");
        System.out.println("Put T1...Done");
        //
        Thread.sleep(1_000);
        //
        System.out.println("Get T1...Starting");
        System.out.println("Get T1: => " + t1.get("key1"));
        System.out.println("Get T1...Done");
        //
        System.out.println("Get T2...Starting");
        System.out.println("Get T2: => " + t2.get("key1"));
        System.out.println("Get T2...Done");
        //
        System.out.println("Put T1");
        t1.put("key1", "value2");
        System.out.println("Put T1...Done");
        //
        Thread.sleep(1_000);
        //
        System.out.println("Get T1...Starting");
        System.out.println("Get T1: => " + t1.get("key1"));
        System.out.println("Get T1...Done");
        //
        System.out.println("Get T2...Starting");
        System.out.println("Get T2: => " + t2.get("key1"));
        System.out.println("Get T2...Done");
        //
        System.exit(0);
    }
}
