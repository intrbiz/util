package com.intrbiz.scripting;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class RestrictedScriptEngineFactory implements ScriptEngineFactory
{
    private final ScriptEngineFactory factory;
    
    private final AccessControlContext context;
    
    public RestrictedScriptEngineFactory(ScriptEngineFactory factory, AccessControlContext context)
    {
        super();
        this.factory = factory;
        this.context = context;
    }

    @Override
    public String getEngineName()
    {
        return this.factory.getEngineName();
    }

    @Override
    public String getEngineVersion()
    {
        return this.factory.getEngineVersion();
    }

    @Override
    public List<String> getExtensions()
    {
        return this.factory.getExtensions();
    }

    @Override
    public List<String> getMimeTypes()
    {
        return this.factory.getMimeTypes();
    }

    @Override
    public List<String> getNames()
    {
        return this.factory.getNames();
    }

    @Override
    public String getLanguageName()
    {
        return this.factory.getLanguageName();
    }

    @Override
    public String getLanguageVersion()
    {
        return this.factory.getLanguageVersion();
    }

    @Override
    public Object getParameter(String key)
    {
        return this.factory.getParameter(key);
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args)
    {
        return this.factory.getMethodCallSyntax(obj, m, args);
    }

    @Override
    public String getOutputStatement(String toDisplay)
    {
        return this.factory.getOutputStatement(toDisplay);
    }

    @Override
    public String getProgram(String... statements)
    {
        return this.factory.getProgram(statements);
    }

    @Override
    public ScriptEngine getScriptEngine()
    {
        return AccessController.doPrivileged(new PrivilegedAction<ScriptEngine>()
        {
            @Override
            public ScriptEngine run()
            {
                ScriptEngine engine = factory.getScriptEngine();
                return engine == null ? null : new RestrictedScriptEngine(engine, context);
            }
        }, 
        this.context);
    }
}
