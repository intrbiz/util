package com.intrbiz.scripting;

import javax.script.ScriptEngine;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestRestrictedScriptManager
{
    @Test
    public void testCanCreateNashornEngine()
    {
        RestrictedScriptManager manager = new RestrictedScriptManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        assertTrue("Can create nashorn script engine", engine != null);
    }
    
    @Test
    public void testCannotCreateBlahBlahEngine()
    {
        RestrictedScriptManager manager = new RestrictedScriptManager();
        ScriptEngine engine = manager.getEngineByName("blahblah");
        assertTrue("Can create blahblah script engine", engine == null);
    }
}
