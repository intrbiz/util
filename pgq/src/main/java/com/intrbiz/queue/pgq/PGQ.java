package com.intrbiz.queue.pgq;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.intrbiz.util.pool.database.DatabasePool;

public abstract class PGQ<T> implements Runnable
{   
    private static final Logger logger = Logger.getLogger(PGQ.class);
    
    protected final DatabasePool pool;

    protected final String queueName;

    protected final String consumerName;
    
    protected ConcurrentMap<String, PGQConsumer<T>> pGQConsumers = new ConcurrentHashMap<String, PGQConsumer<T>>();

    protected boolean run = false;

    protected long pollDelay;

    public PGQ(DatabasePool pool, String queueName, String consumerName)
    {
        super();
        this.pollDelay = 5_000;
        this.pool = pool;
        this.queueName = queueName;
        this.consumerName = consumerName;
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
            public void put(T event) throws PGQueueException
            {
                Objects.requireNonNull(event, "Event cannot be null");
                this.put(PGQ.this.toEventType(event), event, null, null, null, null);
            }
            
            public void put(String type, T event, String extra1, String extra2, String extra3, String extra4) throws PGQueueException
            {
                Objects.requireNonNull(event, "Event cannot be null");
                // Encode the event
                String eventData = PGQ.this.encodeEvent(event);
                // Connect to the database
                try (Connection con = PGQ.this.pool.connect())
                {
                    try (PreparedStatement stmt = con.prepareStatement("SELECT pgq.insert_event(?::TEXT, ?::TEXT, ?::TEXT, ?::TEXT, ?::TEXT, ?::TEXT, ?::TEXT)"))
                    {
                        stmt.setString(1, PGQ.this.queueName);
                        stmt.setString(2, type);
                        stmt.setString(3, eventData);
                        stmt.setString(4, extra1);
                        stmt.setString(5, extra2);
                        stmt.setString(6, extra3);
                        stmt.setString(7, extra4);
                        stmt.execute();
                    }
                }
                catch (Exception e)
                {
                    throw new PGQueueException("Failed to insert event onto PGQ", e);
                }
            }
        };
    }
    
    protected abstract String toEventType(T event) throws PGQueueException;

    protected abstract String encodeEvent(T event) throws PGQueueException;

    protected abstract T decodeEvent(String eventType, String data) throws PGQueueException;

    protected boolean consumeEvents()
    {
        // Connect to the DBdecodeEvent
        try (Connection con = this.pool.connect())
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
        catch (Exception e)
        {
            logger.error("Error consuming events", e);
        }
        return true;
    }

    protected void consumeBatch(Connection con, long batchId) throws Exception
    {
        try (PreparedStatement stmt = con.prepareStatement("SELECT ev_id, ev_time, ev_txid, ev_retry, ev_type, ev_data, ev_extra1, ev_extra2, ev_extra3, ev_extra4 FROM pgq.get_batch_events(?::BIGINT)"))
        {
            stmt.setLong(1, batchId);
            try (ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    long evId = rs.getLong(1);
                    Timestamp evTime = rs.getTimestamp(2);
                    long txId = rs.getLong(3);
                    int retry = rs.getInt(4);
                    String evType = rs.getString(5);
                    String evData = rs.getString(6);
                    String extra1 = rs.getString(7);
                    String extra2 = rs.getString(8);
                    String extra3 = rs.getString(9);
                    String extra4 = rs.getString(10);
                    // decode the event
                    T event = this.decodeEvent(evType, evData);
                    // consume the event
                    try
                    {
                        this.consumeEvent(new EventContainer<T>(this, evId, evTime, txId, retry, evType, event, extra1, extra2, extra3, extra4));
                    }
                    catch (Exception e)
                    {
                        logger.error("Failed to consume event", e);
                        // TODO: fail queue
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
}
