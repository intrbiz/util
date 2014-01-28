package com.intrbiz.queue;

public interface QueueEventTranscoder<T>
{
    byte[] encodeAsBytes(T event) throws QueueException;
    
    T decodeFromBytes(byte[] data) throws QueueException;
}
