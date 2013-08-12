package com.intrbiz.data.db.compiler.util;

import java.util.List;

import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.Schema;

public class SQLCommand
{
    private final StringBuilder buffer = new StringBuilder();

    public SQLCommand()
    {
        super();
    }
    
    public SQLCommand(String cmd)
    {
        this.buffer.append(cmd);
    }

    public SQLCommand write(String text)
    {
        this.buffer.append(text);
        return this;
    }

    public SQLCommand writeln(String text)
    {
        this.buffer.append(text);
        this.buffer.append("\r\n");
        return this;
    }

    public SQLCommand writeln()
    {
        this.buffer.append("\r\n");
        return this;
    }

    public SQLCommand writeid(String text)
    {
        this.buffer.append("\"");
        this.buffer.append(text);
        this.buffer.append("\"");
        return this;
    }

    public SQLCommand writeid(Schema schema, String text)
    {
        if (schema != null) this.writeid(schema.getName()).write(".");
        return this.writeid(text);
    }

    public SQLCommand writeColumnNameList(List<Column> cols)
    {
        boolean ns = false;
        for (Column col : cols)
        {
            if (ns) this.write(", ");
            this.writeid(col.getName());
            ns = true;
        }
        return this;
    }
    
    public SQLCommand writeArgumentNameList(List<Argument> args)
    {
        boolean ns = false;
        for (Argument arg : args)
        {
            if (ns) this.write(", ");
            this.writeid("p_" + arg.getName());
            ns = true;
        }
        return this;
    }
    
    public SQLCommand writeArgumentTypeList(List<Argument> args)
    {
        boolean ns = false;
        for (Argument arg : args)
        {
            if (ns) this.write(", ");
            this.write(arg.getType().getSQLType());
            ns = true;
        }
        return this;
    }
    
    public String toString()
    {
        return this.buffer.toString();
    }
}
