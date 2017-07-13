package com.intrbiz.queue.hcq;

import java.net.URI;

import com.intrbiz.hcq.client.HCQClient;
import com.intrbiz.hcq.client.HCQClientProvider;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueException;

public class HCQPool implements QueueBrokerPool<HCQClient>
{   
    public static final String TYPE = "hcq";
    
    private final HCQClientProvider provider;
    
    private final String clientApplication;

    public HCQPool(final String[] servers, final String clientApplication)
    {
        this.clientApplication = clientApplication;
        try
        {
            URI[] uris = new URI[servers.length];
            for (int i = 0; i < servers.length; i++)
            {
                uris[i] = new URI(servers[i]);
            }
            this.provider = new HCQClientProvider(uris);
        }
        catch (Exception e)
        {
            throw new QueueException("Failed to create HCQ provider", e);
        }
    }
    
    public HCQPool(final String server, final String clientApplication)
    {
        this(new String[] { server }, clientApplication);
    }

    @Override
    public HCQClient connect()
    {
        try
        {
            return this.provider.connect(this.clientApplication);
        }
        catch (Exception e)
        {
            throw new QueueException("Failed to create HCQ client", e);
        }
    }

    @Override
    public void close()
    {
        this.provider.terminateAllClients();
    }
    
    @Override
    public String type()
    {
        return TYPE;
    }
}
