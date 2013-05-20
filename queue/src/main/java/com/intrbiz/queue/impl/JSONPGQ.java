package com.intrbiz.queue.impl;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intrbiz.util.pool.database.DatabasePool;

public class JSONPGQ<T> extends PGQ<T>
{
    private JsonFactory factory = new JsonFactory();
    
    public JSONPGQ(DatabasePool pool, String queueName, String consumerName, Class<?>[] eventTypes)
    {
        super(pool, queueName, consumerName, eventTypes);
    }

    @Override
    protected String encodeEvent(T event)
    {
        //
        try
        {
            StringWriter sw = new StringWriter();
            ObjectMapper m = new ObjectMapper();
            JsonGenerator jg = this.factory.createGenerator(sw);
            jg.setPrettyPrinter(new DefaultPrettyPrinter());
            try
            {
                m.writeValue(jg, event);
            }
            finally
            {
                jg.close();
            }
            return sw.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected T decodeEvent(Class<? extends T> type, String data)
    {
        try
        {
            ObjectMapper m = new ObjectMapper();
            JsonParser jp = this.factory.createParser(data);
            try
            {
                return (T) m.readValue(jp, type);
            }
            finally
            {
                jp.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
