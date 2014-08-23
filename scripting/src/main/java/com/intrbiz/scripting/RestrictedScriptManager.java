package com.intrbiz.scripting;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;


/**
 * Wrap ScriptManager to ensure that all Script engines are wrapped with RestrictedScriptEngine.
 * 
 * This is designed to be a drop in replacement for ScriptManager which will execute 
 * ScriptEngines with a restricted Java Security Policy.
 * 
 */
public class RestrictedScriptManager extends ScriptEngineManager
{
    static
    {
        // ensure the Java security manager is loaded
        try
        {
            if (System.getSecurityManager() == null)
            {
                System.setSecurityManager(new SecurityManager());
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load the Java Security Manager!", e);
        }
    }
    
    private final ScriptEngineManager manager;
    
    private final CodeSource source;
    
    private final PermissionCollection permissions;
    
    private final ProtectionDomain domain;
    
    private final AccessControlContext context;
    
    public RestrictedScriptManager(CodeSource source, PermissionCollection permissions)
    {
        super();
        this.manager = new ScriptEngineManager();
        this.source = source;
        this.permissions = permissions;
        this.domain = new ProtectionDomain(this.source, this.permissions);
        this.context = new AccessControlContext(new ProtectionDomain[] { this.domain });
    }
    
    public RestrictedScriptManager(PermissionCollection permissions)
    {
        this(new CodeSource(null, (CodeSigner[]) null), permissions);
    }
    
    public RestrictedScriptManager()
    {
        this(new RestrictedScriptPermissions());
    }

    @Override
    public void setBindings(Bindings bindings)
    {
        this.manager.setBindings(bindings);
    }

    @Override
    public Bindings getBindings()
    {
        return this.manager.getBindings();
    }

    @Override
    public void put(String key, Object value)
    {
        this.manager.put(key, value);
    }

    @Override
    public Object get(String key)
    {
        return this.manager.get(key);
    }

    @Override
    public ScriptEngine getEngineByName(final String shortName)
    {
        return AccessController.doPrivileged(new PrivilegedAction<ScriptEngine>()
        {
            @Override
            public ScriptEngine run()
            {
                ScriptEngine engine = manager.getEngineByName(shortName);
                return engine == null ? null : new RestrictedScriptEngine(engine, context);
            }
        }, 
        this.context);
    }
    
    @Override
    public ScriptEngine getEngineByExtension(final String extension)
    {
        return AccessController.doPrivileged(new PrivilegedAction<ScriptEngine>()
        {
            @Override
            public ScriptEngine run()
            {
                ScriptEngine engine = manager.getEngineByExtension(extension);
                return engine == null ? null : new RestrictedScriptEngine(engine, context);
            }
        }, 
        this.context);
        
    }

    @Override
    public ScriptEngine getEngineByMimeType(final String mimeType)
    {
        return AccessController.doPrivileged(new PrivilegedAction<ScriptEngine>()
        {
            @Override
            public ScriptEngine run()
            {
                ScriptEngine engine = manager.getEngineByMimeType(mimeType);
                return engine == null ? null : new RestrictedScriptEngine(engine, context);
            }
        }, 
        this.context);
    }

    @Override
    public List<ScriptEngineFactory> getEngineFactories()
    {
        return this.manager.getEngineFactories().stream()
                .map((f) -> { return new RestrictedScriptEngineFactory(f, context); })
                .collect(Collectors.toList());
    }

    @Override
    public void registerEngineName(String name, ScriptEngineFactory factory)
    {
        this.manager.registerEngineName(name, factory);
    }

    @Override
    public void registerEngineMimeType(String type, ScriptEngineFactory factory)
    {
        this.manager.registerEngineMimeType(type, factory);
    }

    @Override
    public void registerEngineExtension(String extension, ScriptEngineFactory factory)
    {
        this.manager.registerEngineExtension(extension, factory);
    }

    @Override
    public String toString()
    {
        return "RestrictedScriptManager { permissions => " + this.permissions + "; manager => "  + manager.toString() + "}";
    }
}
