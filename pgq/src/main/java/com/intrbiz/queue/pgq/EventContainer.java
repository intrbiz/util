package com.intrbiz.queue.pgq;

import java.sql.Timestamp;

public class EventContainer<T>
{
    private final PGQ<T> queue;

    private final long eventId;

    private final Timestamp eventTime;

    private final long txId;

    private final int retry;

    private final String eventType;

    private final T event;

    private final String extra1;

    private final String extra2;

    private final String extra3;

    private final String extra4;

    public EventContainer(PGQ<T> queue, long eventId, Timestamp eventTime, long txId, int retry, String eventType, T event, String extra1, String extra2, String extra3, String extra4)
    {
        super();
        this.queue = queue;
        this.eventId = eventId;
        this.txId = txId;
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.event = event;
        this.extra1 = extra1;
        this.extra2 = extra2;
        this.extra3 = extra3;
        this.extra4 = extra4;
        this.retry = retry;
    }

    public PGQ<T> getQueue()
    {
        return queue;
    }

    public Timestamp getEventTime()
    {
        return eventTime;
    }

    public String getEventType()
    {
        return eventType;
    }

    public T getEvent()
    {
        return event;
    }

    /**
     * TODO
     */
    public void retry()
    {
    }
    
    /**
     * TODO
     */
    public void fail()
    {
    }

    public String getExtra1()
    {
        return extra1;
    }

    public String getExtra2()
    {
        return extra2;
    }

    public String getExtra3()
    {
        return extra3;
    }

    public String getExtra4()
    {
        return extra4;
    }

    public long getEventId()
    {
        return eventId;
    }

    public long getTxId()
    {
        return txId;
    }

    public int getRetry()
    {
        return retry;
    }

    public String toString()
    {
        return "EventContiner[type=" + this.eventType + ",id=" + this.eventId + ",txid=" + this.txId + ",extra1=" + this.extra1 + ",extra2=" + this.extra2 + ",extra3=" + this.extra3 + ",extra4=" + this.extra4 + "]{" + this.event + "}";
    }
}
