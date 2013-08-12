package com.intrbiz.data.db.compiler.util;

import java.util.LinkedList;
import java.util.List;

public class SQLScriptSet
{
    private List<SQLScript> scripts = new LinkedList<SQLScript>();
    
    public SQLScriptSet()
    {
        super();
    }
    
    public void add(SQLCommand... commands)
    {
        this.add(new SQLScript(commands));
    }
    
    public void add(String... commands)
    {
        this.add(new SQLScript(commands));
    }
    
    public void add(SQLScript script)
    {
        this.scripts.add(script);
    }

    public List<SQLScript> getScripts()
    {
        return scripts;
    }

    public void setScripts(List<SQLScript> scripts)
    {
        this.scripts = scripts;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (SQLScript s : this.scripts)
        {
            sb.append(s).append("\r\n\r\n");
        }
        return sb.toString();
    }
    
}
