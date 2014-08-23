package com.intrbiz.scripting;

import static org.junit.Assert.*;

import java.security.AccessControlException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Before;
import org.junit.Test;

public class TestRestrictedScripts
{
    @Before
    public void setupSecurityManager()
    {
        // only setup the security manager if it is not already loaded
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManager());
        }
    }
    
    @Test
    public void testJavaSecuirtyManager()
    {
        assertTrue("Java Security Manager is enabled", System.getSecurityManager() != null);
    }
    
    @Test
    public void testPreventExit()
    {
        ScriptEngineManager manager = new RestrictedScriptManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        try
        {
            engine.eval("exit(1);");
            fail("Exit didn't happen, yet a AccessControlException was not raised");
        }
        catch (Exception e)
        {
            assertTrue("Caught access exception when trying to call exit()", e instanceof AccessControlException);
        }
    }

    @Test
    public void testSetProperty()
    {
        ScriptEngineManager manager = new RestrictedScriptManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        try
        {
            engine.eval("java.lang.System.setProperty('some.property', 'Some value');");
            fail("Setting of system properties was permitted");
        }
        catch (Exception e)
        {
            assertTrue("Caught access exception when trying to set property", e instanceof AccessControlException);
        }
    }
    
    @Test
    public void testListDirectory()
    {
        ScriptEngineManager manager = new RestrictedScriptManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        try
        {
            engine.eval(
                "var files = new java.io.File('/').listFiles();"
            );
            fail("Listing of files was permitted");
        }
        catch (Exception e)
        {
            assertTrue("Caught access exception when trying to list files", e instanceof AccessControlException);
        }
    }
    
    @Test
    public void testOpenSocket()
    {
        ScriptEngineManager manager = new RestrictedScriptManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        try
        {
            engine.eval(
                "var s = new java.net.Socket('www.google.com', 80);"
            );
            fail("Opening a socket was permitted");
        }
        catch (Exception e)
        {
            assertTrue("Caught access exception when trying to open a socket", e instanceof AccessControlException);
        }
    }
}
