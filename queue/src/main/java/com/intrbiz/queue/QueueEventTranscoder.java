package com.intrbiz.queue;

import java.util.Base64;

public interface QueueEventTranscoder<T>
{
    byte[] encodeAsBytes(T event) throws QueueException;
    
    T decodeFromBytes(byte[] data) throws QueueException;
    
    default String encodeAsString(T event) throws QueueException
    {
        return event == null ? null : Base64.getEncoder().encodeToString(this.encodeAsBytes(event));
    }
    
    default T decodeFromString(String data) throws QueueException
    {
        return data == null ? null : this.decodeFromBytes(Base64.getDecoder().decode(data));
    }
    
    default String getContentType(T event)
    {
        return "application/json";
    }
}
