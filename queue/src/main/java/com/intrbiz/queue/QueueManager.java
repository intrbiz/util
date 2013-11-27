package com.intrbiz.queue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manage QueueAdapter implementation and broker connections
 */
public class QueueManager
{
    private static final QueueManager US = new QueueManager();
    
    public static final QueueManager getInstance()
    {
        return US;
    }
    
    private QueueBrokerPool<?> defaultBroker;
    
    private final ConcurrentMap<String, QueueBrokerPool<?>> brokers = new ConcurrentHashMap<String, QueueBrokerPool<?>>();
    
    private final ConcurrentMap<Class<? extends QueueAdapter>, QueueAdapterFactory<?>> queueAdapters = new ConcurrentHashMap<Class<? extends QueueAdapter>, QueueAdapterFactory<?>>();
    
    private QueueManager()
    {
    }
    
    // default broker
    
    public void registerDefaultBroker(QueueBrokerPool<?> broker)
    {
        this.defaultBroker = broker;
    }
    
    public QueueBrokerPool<?> defaultBroker()
    {
        return this.defaultBroker;
    }
    
    // brokers
    
    public void registerBroker(String name, QueueBrokerPool<?> broker)
    {
        this.brokers.put(name, broker);
    }
    
    public QueueBrokerPool<?> broker(String name)
    {
        return this.brokers.get(name);
    }
    
    // adapters
    
    public <T extends QueueAdapter> void registerQueueAdapter(Class<T> cls, QueueAdapterFactory<T> factory)
    {
        this.queueAdapters.put(cls, factory);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends QueueAdapter> T queueAdapter(Class<T> cls, QueueBrokerPool<?> broker)
    {
        QueueAdapterFactory<?> factory = this.queueAdapters.get(cls);
        if (factory == null) throw new RuntimeException("No implementation is registered for " + cls.getName());
        return (T) factory.create(broker);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends QueueAdapter> T queueAdapter(Class<T> cls)
    {
        if (this.defaultBroker == null) throw new RuntimeException("Cannot connect to the default broker, it is not registered");
        QueueAdapterFactory<?> factory = this.queueAdapters.get(cls);
        if (factory == null) throw new RuntimeException("No implementation is registered for " + cls.getName());
        return (T) factory.create(this.defaultBroker);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends QueueAdapter> T queueAdapter(Class<T> cls, String broker)
    {
        QueueBrokerPool<?> brokerPool = this.broker(broker);
        if (broker == null) throw new RuntimeException("Cannot connect to the " + broker + " broker, it is not registered");
        QueueAdapterFactory<?> factory = this.queueAdapters.get(cls);
        if (factory == null) throw new RuntimeException("No implementation is registered for " + cls.getName());
        return (T) factory.create(brokerPool);
    }
    
    public static interface QueueAdapterFactory<T extends QueueAdapter>
    {
        T create(QueueBrokerPool<?> broker);
    }
}
