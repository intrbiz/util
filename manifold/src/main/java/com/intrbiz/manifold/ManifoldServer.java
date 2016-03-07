package com.intrbiz.manifold;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.intrbiz.Util;
import com.intrbiz.manifold.metadata.ConsumeQueue;
import com.intrbiz.manifold.model.Messagelet;
import com.intrbiz.manifold.model.MessageletContext;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueAdapter;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.QueueManager;

public class ManifoldServer
{    
    private ConcurrentMap<UUID, MessageletInstance> messagelets = new ConcurrentHashMap<UUID, MessageletInstance>();
    
    public ManifoldServer()
    {
        super();
    }
    
    public UUID start(final Class<? extends Messagelet<?>> messageletType, final Object... arguments)
    {
        return this.start(messageletType, null, arguments);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public UUID start(final Class<? extends Messagelet<?>> messageletType, final String broker, final Object... arguments)
    {
        try
        {
            final UUID id = UUID.randomUUID();
            // what queue does this messagelet want to consume from
            ConsumeQueue cq = messageletType.getAnnotation(ConsumeQueue.class);
            if (cq == null) throw new QueueException("Cannot start Messagelet " + messageletType.getSimpleName() + " it is missing a ConsumeQueue annotation");
            // find the consumer
            Method consumerFactory = this.findConsumer(cq, arguments);
            if (consumerFactory == null) throw new QueueException("Cannot find the consumer " + cq.consume() + " of " + cq.queue().getSimpleName()); 
            // get the queue
            QueueAdapter queue = broker == null ? QueueManager.getInstance().queueAdapter(cq.queue()) : QueueManager.getInstance().queueAdapter(cq.queue(), broker); 
            // create the messagelet
            Messagelet<?> messagelet = messageletType.newInstance();
            // setup the messagelet
            messagelet.setup(new MessageletContext()
            {
                @Override
                public UUID id()
                {
                    return id;
                }

                @Override
                public <Q extends QueueAdapter> Q queue()
                {
                    return (Q) queue;
                }

                @Override
                public <Q extends QueueAdapter> Q queue(Class<Q> queueType)
                {
                    return broker == null ? QueueManager.getInstance().queueAdapter(queueType) : QueueManager.getInstance().queueAdapter(queueType, broker);
                }

                @Override
                public Object[] consumerArguments()
                {
                    return arguments;
                }
            });
            // handler
            DeliveryHandler<?> handler = ((Messagelet) messagelet)::consume;
            // setup the consumer
            // the first argument to the factory must be the handler
            Object[] factoryArguments = new Object[(arguments == null ? 0 : arguments.length) + 1];
            factoryArguments[0] = handler;
            if (arguments != null) System.arraycopy(arguments, 0, factoryArguments, 1, arguments.length);
            Consumer<?,?> consumer = (Consumer<?,?>) consumerFactory.invoke(queue, factoryArguments);
            // register
            this.messagelets.put(id, new MessageletInstance(id, broker, queue, consumer, arguments, messagelet));
            // done
            return id;
        }
        catch (QueueException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new QueueException("Failed to start Messagelet", e);
        }
    }
    
    protected Method findConsumer(ConsumeQueue cq, Object[] arguments) throws Exception
    {
        // the method name
        String name = "consume" + Util.ucFirst(cq.consume());
        // find the method
        for (Method method : cq.queue().getMethods())
        {
            if (name.equals(method.getName()))
                return method;
        }
        return null;
    }
    
    protected MessageletInstance getMessagelet(UUID id)
    {
        return this.messagelets.get(id);
    }
    
    protected static class MessageletInstance
    {
        public final UUID id;
        
        public final String broker;
        
        public final QueueAdapter queue;
        
        public final Consumer<?,?> consumer;
        
        public final Object[] arguments;
        
        public final Messagelet<?> messagelet;

        public MessageletInstance(UUID id, String broker, QueueAdapter queue, Consumer<?,?> consumer, Object[] arguments, Messagelet<?> messagelet)
        {
            super();
            this.id = id;
            this.broker = broker;
            this.queue = queue;
            this.consumer = consumer;
            this.arguments = arguments;
            this.messagelet = messagelet;
        }
    }
}
