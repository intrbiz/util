package com.intrbiz.queue;




public interface Consumer<T> extends AutoCloseable
{
    String name();
    
    DeliveryHandler<T> handler();
    
    void addBinding(String binding);
    
    @Override
    void close();
}
