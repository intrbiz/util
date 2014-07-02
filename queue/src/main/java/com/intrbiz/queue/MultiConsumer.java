package com.intrbiz.queue;

public interface MultiConsumer<T> extends Consumer<T>
{
    String[] names();
}
