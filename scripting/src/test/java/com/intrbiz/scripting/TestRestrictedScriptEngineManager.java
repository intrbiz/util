package com.intrbiz.scripting;

import javax.script.ScriptEngine;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestRestrictedScriptEngineManager
{
    @Test
    public void testCanCreateNashornEngine()
    {
        RestrictedScriptEngineManager manager = new RestrictedScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        assertTrue("Can create nashorn script engine", engine != null);
    }
    
    @Test
    public void testCannotCreateBlahBlahEngine()
    {
        RestrictedScriptEngineManager manager = new RestrictedScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("blahblah");
        assertTrue("Can create blahblah script engine", engine == null);
    }
}
