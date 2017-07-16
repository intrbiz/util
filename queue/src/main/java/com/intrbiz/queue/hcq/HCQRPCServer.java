package com.intrbiz.queue.hcq;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.hcq.client.HCQBatch;
import com.intrbiz.hcq.client.HCQClient;
import com.intrbiz.hcq.model.message.queue.ReceiveMessageFromQueue;
import com.intrbiz.hcq.model.message.type.QueuedMessage;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.RPCHandler;
import com.intrbiz.queue.RPCServer;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.Queue;
import com.intrbiz.queue.name.RoutingKey;

public class HCQRPCServer<T, R> extends HCQBase<T> implements RPCServer<T, R>
{
    private static final Logger logger = Logger.getLogger(HCQRPCServer.class);

    private final RPCHandler<T, R> handler;

    private final Exchange exchange;

    private final Queue requestQueueSpec;

    private final RoutingKey[] bindings;

    private Queue requestQueue;
    
    private final QueueEventTranscoder<R> responseTranscoder;
    
    private final int capacity;

    public HCQRPCServer(QueueBrokerPool<HCQClient> broker, QueueEventTranscoder<T> transcoder, QueueEventTranscoder<R> responseTranscoder, RPCHandler<T,R> handler, Queue requestQueue, int capacity, Exchange exchange, RoutingKey... bindings)
    {
        super(broker, transcoder);
        this.responseTranscoder = responseTranscoder;
        this.handler = handler;
        this.requestQueueSpec = requestQueue;
        this.exchange = exchange;
        this.bindings = bindings;
        this.capacity = capacity;
        this.init();
    }

    protected void setup() throws Exception
    {
        HCQBatch batch = this.client.batch();
        // declare the queue
        if (this.requestQueueSpec == null)
        {
            // declare a temporary queue
            this.requestQueue = new Queue("rpc-server-" + UUID.randomUUID(), false);
            batch.getOrCreateTempQueue(this.requestQueue.getName(), this.capacity);
        }
        else
        {
            // use the name given
            this.requestQueue = this.requestQueueSpec;
            batch.getOrCreateQueue(this.requestQueue.getName(), this.capacity, ! this.requestQueue.isPersistent());
        }
        // declare the exchange
        batch.getOrCreateExchange(this.exchange.getName(), this.exchange.getType());
        // bind our queue to the exchange with the given routing keys
        if (this.bindings != null && this.bindings.length > 0)
        {
            for (RoutingKey binding : this.bindings)
            {
                batch.bindQueueToExchange(this.exchange.getName(), binding.toString(), this.requestQueue.getName());
            }
        }
        else
        {
            batch.bindQueueToExchange(this.exchange.getName(), this.requestQueue.getName(), this.requestQueue.getName());
        }
        // submit the batch
        batch.submit().sync();
        // start consuming
        this.client.startConsumingQueue(this.requestQueue.getName(), this::handleDelivery).sync();
    }

    protected void handleDelivery(ReceiveMessageFromQueue message)
    {
        try
        {
            QueuedMessage theMessage = message.getMessage(); 
            String correlationId = theMessage.getCorrelationId();
            String replyTo = theMessage.getReplyTo();
            if (correlationId != null && replyTo != null)
            {
                // decode the event
                T event = this.transcoder.decodeFromString(theMessage.getPayload());
                // handle the event
                R response = this.handler.handleDevliery(event);
                // send the response
                if (response != null)
                {
                    String contentType = this.responseTranscoder.getContentType(response);
                    String payload = this.responseTranscoder.encodeAsString(response);
                    this.client.publishMessageToQueue(
                            replyTo,
                            new QueuedMessage().randomMessageId().correlationId(correlationId).payload(contentType, payload)
                    ).sync();
                }
            }
            else
            {
                logger.warn("Ignoring request without correlationId or replyTo");
            }
        }
        catch (Exception e)
        {
            logger.error("Error processing RPC request", e);
        }
    }

    @Override
    public String name()
    {
        return null;
    }

    @Override
    public Queue requestQueue()
    {
        return this.requestQueue;
    }

    @Override
    public RPCHandler<T, R> handler()
    {
        return this.handler;
    }
}
