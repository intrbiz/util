package com.intrbiz.data.db.compiler.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

public class SQLScript
{
    private List<SQLCommand> commands = new LinkedList<SQLCommand>();

    public SQLScript()
    {
        super();
    }
    
    public SQLScript(SQLCommand... cmds)
    {
        for (SQLCommand cmd : cmds)
        {
            this.commands.add(cmd);
        }
    }
    
    public SQLScript(String... cmds)
    {
        for (String cmd : cmds)
        {
            this.commands.add(new SQLCommand(cmd));
        }
    }

    public SQLCommand command()
    {
        SQLCommand cmd = new SQLCommand();
        this.commands.add(cmd);
        return cmd;
    }

    public List<SQLCommand> getCommands()
    {
        return commands;
    }

    public void setCommands(List<SQLCommand> commands)
    {
        this.commands = commands;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        //
        for (SQLCommand cmd : this.commands)
        {
            sb.append(cmd.toString()).append(";\r\n\r\n");
        }
        //
        return sb.toString();
    }
    
    public static final SQLScript fromResource(Class<?> adapterCls, String resourceName)
    {
        try (Reader r = new BufferedReader(new InputStreamReader(adapterCls.getResourceAsStream(resourceName))))
        {
            StringBuilder sb = new StringBuilder();
            int l;
            char[] b = new char[1024];
            while ((l = r.read(b)) != -1)
            {
                sb.append(b, 0, l);
            }
            return new SQLScript(sb.toString());
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load the resource \"" + resourceName, e);
        }
    }
}
