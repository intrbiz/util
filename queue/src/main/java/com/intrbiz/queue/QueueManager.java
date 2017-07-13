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
    
    private final ConcurrentMap<Class<? extends QueueAdapter>, ConcurrentMap<String, QueueAdapterFactory<?>>> queueAdapters = new ConcurrentHashMap<Class<? extends QueueAdapter>, ConcurrentMap<String, QueueAdapterFactory<?>>>();
    
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
        return name == null ? this.defaultBroker : this.brokers.get(name);
    }
    
    // adapters
    
    public <T extends QueueAdapter> void registerQueueAdapter(Class<T> cls, String type, QueueAdapterFactory<T> factory)
    {
        ConcurrentMap<String, QueueAdapterFactory<?>> impls = this.queueAdapters.computeIfAbsent(cls, (k) -> new ConcurrentHashMap<String, QueueAdapterFactory<?>>());
        impls.put(type, factory);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T extends QueueAdapter> T queueAdapter(Class<T> cls, QueueBrokerPool<?> broker)
    {
        ConcurrentMap<String, QueueAdapterFactory<?>> impls = this.queueAdapters.get(cls);
        if (impls == null) throw new QueueException("No implementation is registered for " + cls.getName() + "::" + broker.type());
        QueueAdapterFactory factory = impls.get(broker.type());
        if (factory == null) throw new QueueException("No implementation is registered for " + cls.getName() + "::" + broker.type());
        return (T) factory.create(broker);
    }
    
    public <T extends QueueAdapter> T queueAdapter(Class<T> cls)
    {
        if (this.defaultBroker == null) throw new QueueException("Cannot connect to the default broker, it is not registered");
        return this.queueAdapter(cls, this.defaultBroker);
    }
    
    public <T extends QueueAdapter> T queueAdapter(Class<T> cls, String broker)
    {
        QueueBrokerPool<?> brokerPool = this.broker(broker);
        if (broker == null) throw new RuntimeException("Cannot connect to the " + broker + " broker, it is not registered");
        return this.queueAdapter(cls, brokerPool);
    }
    
    @FunctionalInterface
    public static interface QueueAdapterFactory<T extends QueueAdapter>
    {
        T create(QueueBrokerPool<?> broker);
    }
}
