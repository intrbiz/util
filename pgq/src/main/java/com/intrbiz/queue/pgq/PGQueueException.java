package com.intrbiz.queue.pgq;

public class PGQueueException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public PGQueueException()
    {
        super();
    }

    public PGQueueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PGQueueException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PGQueueException(String message)
    {
        super(message);
    }

    public PGQueueException(Throwable cause)
    {
        super(cause);
    }
}
