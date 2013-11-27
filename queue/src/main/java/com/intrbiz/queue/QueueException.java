package com.intrbiz.queue;

public class QueueException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public QueueException()
    {
        super();
    }

    public QueueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public QueueException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public QueueException(String message)
    {
        super(message);
    }

    public QueueException(Throwable cause)
    {
        super(cause);
    }
}
