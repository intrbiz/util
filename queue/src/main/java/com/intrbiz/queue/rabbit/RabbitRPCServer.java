package com.intrbiz.queue.rabbit;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.RPCHandler;
import com.intrbiz.queue.RPCServer;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.Queue;
import com.intrbiz.queue.name.RoutingKey;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RabbitRPCServer<T, R> extends RabbitBase<T> implements RPCServer<T, R>
{
    private Logger logger = Logger.getLogger(RabbitRPCServer.class);

    private final RPCHandler<T, R> handler;

    private final Exchange exchange;

    private final Queue requestQueueSpec;

    private final RoutingKey[] bindings;

    private Queue requestQueue;

    private String consumerName;
    
    private final QueueEventTranscoder<R> responseTranscoder;

    public RabbitRPCServer(QueueBrokerPool<Channel> broker, QueueEventTranscoder<T> transcoder, QueueEventTranscoder<R> responseTranscoder, RPCHandler<T,R> handler, Queue requestQueue, Exchange exchange, RoutingKey... bindings)
    {
        super(broker, transcoder);
        this.responseTranscoder = responseTranscoder;
        this.handler = handler;
        this.requestQueueSpec = requestQueue;
        this.exchange = exchange;
        this.bindings = bindings;
        this.init();
    }

    protected void setup() throws IOException
    {
        // declare the queue
        if (this.requestQueueSpec == null)
        {
            // declare a temporary queue
            this.requestQueue = new Queue(this.channel.queueDeclare().getQueue(), false);
        }
        else
        {
            // use the name given
            this.channel.queueDeclare(this.requestQueueSpec.getName(), this.requestQueueSpec.isPersistent(), false, !this.requestQueueSpec.isPersistent(), null);
            this.requestQueue = this.requestQueueSpec;
        }
        // bind the queue
        // declare the exchange
        this.channel.exchangeDeclare(this.exchange.getName(), this.exchange.getType(), this.exchange.isPersistent());
        // bind our queue to the exchange with the given routing keys
        if (this.bindings != null && this.bindings.length > 0)
        {
            for (RoutingKey binding : this.bindings)
            {
                this.channel.queueBind(this.requestQueue.getName(), this.exchange.getName(), binding.toString());
            }
        }
        else
        {
            this.channel.queueBind(this.requestQueue.getName(), this.exchange.getName(), this.requestQueue.toKey().toString());
        }
        // consume
        try
        {
            this.consumerName = this.channel.basicConsume(this.requestQueue.getName(), false, new DefaultConsumer(this.channel)
            {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException
                {
                    try
                    {
                        RabbitRPCServer.this.handleDelivery(consumerTag, envelope, properties, body);
                    }
                    catch (Exception e)
                    {
                        logger.error("Unhandled error handling delivery", e);
                    }
                }
            });
        }
        catch (IOException e)
        {
            throw new QueueException("Cannot declare consumer", e);
        }
    }

    protected void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException
    {
        String correlationId = properties.getCorrelationId();
        String replyTo = properties.getReplyTo();
        if (correlationId != null && replyTo != null)
        {
            // decode the event
            T event = this.transcoder.decodeFromBytes(body);
            // handle the event
            R response = this.handler.handleDevliery(event);
            // ack the event
            this.channel.basicAck(envelope.getDeliveryTag(), false);
            // send the response
            if (response != null)
            {
                this.channel.basicPublish(
                        "", 
                        replyTo, 
                        new BasicProperties("application/json", null, null, 1, null, correlationId, null, null, null, null, null, null, null, null), 
                        this.responseTranscoder.encodeAsBytes(response)
                );
            }
        }
        else
        {
            logger.warn("Ignoring request without correlationId or replyTo");
        }
    }

    @Override
    public String name()
    {
        return this.consumerName;
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
