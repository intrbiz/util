package com.intrbiz.queue.pgq;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.intrbiz.util.pool.database.DatabasePool;

public abstract class PGQ<T> implements Runnable
{
    protected final DatabasePool pool;

    protected final Class<?>[] eventTypes;

    protected final String queueName;

    protected final String consumerName;
    
    protected ConcurrentMap<String, PGQConsumer<T>> pGQConsumers = new ConcurrentHashMap<String, PGQConsumer<T>>();

    protected boolean run = false;

    protected long pollDelay;

    public PGQ(DatabasePool pool, String queueName, String consumerName, Class<?>[] eventTypes)
    {
        super();
        this.pollDelay = 5_000;
        this.pool = pool;
        this.queueName = queueName;
        this.consumerName = consumerName;
        this.eventTypes = eventTypes;
    }

    public void newConsumer(PGQConsumer<T> consumer)
    {
        this.pGQConsumers.put(consumer.getName(), consumer);
    }

    protected void consumeEvent(EventContainer<T> event)
    {
        for (PGQConsumer<T> con : pGQConsumers.values())
        {
            con.take(event);
        }
    }

    public void run()
    {
        this.run = true;
        while (this.run)
        {
            if (this.consumeEvents())
            {
                // sleep
                synchronized (this)
                {
                    try
                    {
                        this.wait(this.pollDelay);
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
            }
        }
    }

    public void shutdown()
    {
        this.run = false;
        // wake up if we are sleeping
        synchronized (this)
        {
            this.notifyAll();
        }
    }

    public boolean isRunning()
    {
        return this.run;
    }

    public PGQProducer<T> newProducer()
    {
        return new PGQProducer<T>()
        {
            public void put(T event)
            {
                if (event == null) throw new NullPointerException("Cannot put a null event!");
                // Encode the event to a string
                String eventType = event.getClass().getName();
                String eventData = PGQ.this.encodeEvent(event);
                // Connect to the database
                try (Connection con = PGQ.this.pool.connect())
                {
                    try (PreparedStatement stmt = con.prepareStatement("SELECT pgq.insert_event(?::TEXT, ?::TEXT, ?::TEXT)"))
                    {
                        stmt.setString(1, PGQ.this.queueName);
                        stmt.setString(2, eventType);
                        stmt.setString(3, eventData);
                        stmt.execute();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
    }

    protected abstract String encodeEvent(T event);

    protected abstract T decodeEvent(Class<? extends T> type, String data);

    protected boolean consumeEvents()
    {
        // Connect to the DB
        try (Connection con = this.pool.connect())
        {
            try
            {
                long batchId = this.nextBatch(con);
                // no events to process so lets have a nap
                if (batchId < 1) return true;
                // fetch the batch events
                try
                {
                    this.consumeBatch(con, batchId);
                    // successfully processed a batch of events
                    return false;
                }
                finally
                {
                    // finish the batch
                    this.finishBatch(con, batchId);
                }
            }
            finally
            {
                // TODO temporary bodge
                this.doTick(con);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    protected void consumeBatch(Connection con, long batchId) throws Exception
    {
        try (PreparedStatement stmt = con.prepareStatement("SELECT ev_time, ev_type, ev_data FROM pgq.get_batch_events(?::BIGINT)"))
        {
            stmt.setLong(1, batchId);
            try (ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    Timestamp evTime = rs.getTimestamp(1);
                    Class<? extends T> evClass = (Class<? extends T>) Class.forName(rs.getString(2));
                    String evData = rs.getString(3);
                    // decode the event
                    T event = this.decodeEvent(evClass, evData);
                    // consume the event
                    try
                    {
                        this.consumeEvent(new EventContainer<T>(this, evTime, evClass, event));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        // TODO fail the event
                    }
                }
            }
        }
    }

    protected long nextBatch(Connection con) throws Exception
    {
        try (PreparedStatement stmt = con.prepareStatement("SELECT pgq.next_batch(?::TEXT, ?::TEXT)"))
        {
            stmt.setString(1, this.queueName);
            stmt.setString(2, this.consumerName);
            try (ResultSet rs = stmt.executeQuery())
            {
                if (rs.next())
                {
                    Object v = rs.getObject(1);
                    if (v instanceof Long)
                    {
                        return (Long) v;
                    }
                    else
                    {
                        return -1;
                    }
                }
            }
        }
        return -1;
    }

    protected void finishBatch(Connection con, long batchId) throws Exception
    {
        try (PreparedStatement stmt = con.prepareStatement("SELECT pgq.finish_batch(?::BIGINT)"))
        {
            stmt.setLong(1, batchId);
            stmt.execute();
        }
    }

    protected void doTick(Connection con) throws Exception
    {
        try (PreparedStatement stmt = con.prepareStatement("SELECT pgq.ticker(?::TEXT)"))
        {
            stmt.setString(1, this.queueName);
            stmt.execute();
        }
    }
}
