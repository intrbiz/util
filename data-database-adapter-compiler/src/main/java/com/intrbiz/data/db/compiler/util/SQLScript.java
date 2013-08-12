package com.intrbiz.data.db.compiler.util;

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
}
