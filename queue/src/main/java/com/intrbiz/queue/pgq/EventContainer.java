package com.intrbiz.queue.pgq;

import java.sql.Timestamp;

public class EventContainer<T>
{
    private final PGQ<T> queue;

    private final Timestamp eventTime;

    private final Class<? extends T> eventType;

    private final T event;

    public EventContainer(PGQ<T> queue, Timestamp eventTime, Class<? extends T> eventType, T event)
    {
        super();
        this.queue = queue;
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.event = event;
    }

    public PGQ<T> getQueue()
    {
        return queue;
    }

    public Timestamp getEventTime()
    {
        return eventTime;
    }

    public Class<? extends T> getEventType()
    {
        return eventType;
    }

    public T getEvent()
    {
        return event;
    }
    
    public void retry()
    {
        // TODO
    }
}
