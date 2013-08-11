package com.intrbiz.data.db.compiler.util;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.Schema;

public class SQLWriter
{
    private final Writer writer;

    public SQLWriter(Writer writer)
    {
        this.writer = writer;
    }

    public SQLWriter write(String text) throws IOException
    {
        this.writer.write(text);
        return this;
    }

    public SQLWriter writeln(String text) throws IOException
    {
        this.writer.write(text);
        this.writer.write("\r\n");
        return this;
    }

    public SQLWriter writeln() throws IOException
    {
        this.writer.write("\r\n");
        return this;
    }

    public SQLWriter writeid(String text) throws IOException
    {
        boolean simple = this.isSimpleName(text);
        if (!simple) this.writer.write("\"");
        this.writer.write(text);
        if (!simple) this.writer.write("\"");
        return this;
    }

    public SQLWriter writeid(Schema schema, String text) throws IOException
    {
        if (schema != null) this.writeid(schema.getName()).write(".");
        return this.writeid(text);
    }

    public SQLWriter writeColumnNameList(List<Column> cols) throws IOException
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
    
    public SQLWriter writeArgumentNameList(List<Argument> args) throws IOException
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
    
    public SQLWriter writeArgumentTypeList(List<Argument> args) throws IOException
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

    public boolean isSimpleName(String name)
    {
        return false;
    }

    public void flush()
    {
        try
        {
            this.writer.flush();
        }
        catch (IOException e)
        {
        }
    }

    public void close()
    {
        try
        {
            this.writer.close();
        }
        catch (IOException e)
        {
        }
    }
}
