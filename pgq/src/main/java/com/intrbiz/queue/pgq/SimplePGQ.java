package com.intrbiz.queue.pgq;

import com.intrbiz.util.pool.database.DatabasePool;

/**
 * A simple plain text PGQ
 */
public class SimplePGQ extends PGQ<String>
{
    public SimplePGQ(DatabasePool pool, String queueName, String consumerName)
    {
        super(pool, queueName, consumerName);
    }

    @Override
    protected String toEventType(String event) throws PGQueueException
    {
        return null;
    }

    @Override
    protected String encodeEvent(String event) throws PGQueueException
    {
        return event;
    }

    @Override
    protected String decodeEvent(String eventType, String data) throws PGQueueException
    {
        return data;
    }
}
