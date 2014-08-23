package com.intrbiz.scripting;

import java.io.Reader;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

/**
 * Execute a ScriptEngine with restricted permissions
 */
public class RestrictedScriptEngine implements ScriptEngine
{
    private ScriptEngine engine;

    private AccessControlContext context;

    public RestrictedScriptEngine(ScriptEngine engine, AccessControlContext context)
    {
        super();
        this.engine = engine;
        this.context = context;
    }

    @Override
    public Object eval(final String script, final ScriptContext context) throws ScriptException
    {
        try
        {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
            {
                @Override
                public Object run() throws Exception
                {
                    return engine.eval(script, context);
                }
            }, 
            this.context);
        }
        catch (PrivilegedActionException e)
        {
            throw e.getCause() instanceof ScriptException ? (ScriptException) e.getCause() : new ScriptException(e);
        }
    }

    @Override
    public Object eval(final Reader reader, final ScriptContext context) throws ScriptException
    {
        try
        {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
            {
                @Override
                public Object run() throws Exception
                {
                    return engine.eval(reader, context);
                }
            }, 
            this.context);
        }
        catch (PrivilegedActionException e)
        {
            throw e.getCause() instanceof ScriptException ? (ScriptException) e.getCause() : new ScriptException(e);
        }
    }

    @Override
    public Object eval(final String script) throws ScriptException
    {
        try
        {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
            {
                @Override
                public Object run() throws Exception
                {
                    return engine.eval(script);
                }
            }, 
            this.context);
        }
        catch (PrivilegedActionException e)
        {
            throw e.getCause() instanceof ScriptException ? (ScriptException) e.getCause() : new ScriptException(e);
        }
    }

    @Override
    public Object eval(final Reader reader) throws ScriptException
    {
        try
        {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
            {
                @Override
                public Object run() throws Exception
                {
                    return engine.eval(reader);
                }
            }, 
            this.context);
        }
        catch (PrivilegedActionException e)
        {
            throw e.getCause() instanceof ScriptException ? (ScriptException) e.getCause() : new ScriptException(e);
        }
    }

    @Override
    public Object eval(final String script, final Bindings n) throws ScriptException
    {
        try
        {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
            {
                @Override
                public Object run() throws Exception
                {
                    return engine.eval(script, n);
                }
            }, 
            this.context);
        }
        catch (PrivilegedActionException e)
        {
            throw e.getCause() instanceof ScriptException ? (ScriptException) e.getCause() : new ScriptException(e);
        }
    }

    @Override
    public Object eval(final Reader reader, final Bindings n) throws ScriptException
    {
        try
        {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
            {
                @Override
                public Object run() throws Exception
                {
                    return engine.eval(reader, n);
                }
            }, 
            this.context);
        }
        catch (PrivilegedActionException e)
        {
            throw e.getCause() instanceof ScriptException ? (ScriptException) e.getCause() : new ScriptException(e);
        }
    }

    @Override
    public void put(String key, Object value)
    {
        this.engine.put(key, value);
    }

    @Override
    public Object get(String key)
    {
        return this.engine.get(key);
    }

    @Override
    public Bindings getBindings(int scope)
    {
        return this.engine.getBindings(scope);
    }

    @Override
    public void setBindings(Bindings bindings, int scope)
    {
        this.engine.setBindings(bindings, scope);
    }

    @Override
    public Bindings createBindings()
    {
        return this.engine.createBindings();
    }

    @Override
    public ScriptContext getContext()
    {
        return this.engine.getContext();
    }

    @Override
    public void setContext(ScriptContext context)
    {
        this.engine.setContext(context);
    }

    @Override
    public ScriptEngineFactory getFactory()
    {
        return this.engine.getFactory();
    }
}
