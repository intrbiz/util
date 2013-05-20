package com.intrbiz.queue;

import java.sql.Timestamp;

public class EventContainer<T>
{
    private final Queue<T> queue;

    private final Timestamp eventTime;

    private final Class<? extends T> eventType;

    private final T event;

    public EventContainer(Queue<T> queue, Timestamp eventTime, Class<? extends T> eventType, T event)
    {
        super();
        this.queue = queue;
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.event = event;
    }

    public Queue<T> getQueue()
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
