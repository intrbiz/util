package com.intrbiz.data.db.compiler.model;

import com.intrbiz.data.db.compiler.meta.ScriptType;
import com.intrbiz.data.db.compiler.util.SQLScript;

public class Patch implements Comparable<Patch>
{
    private String name;

    private ScriptType type;

    private int index;

    private Version version;

    private boolean skip;

    private SQLScript script;

    public Patch()
    {
        super();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ScriptType getType()
    {
        return type;
    }

    public void setType(ScriptType type)
    {
        this.type = type;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public Version getVersion()
    {
        return version;
    }

    public void setVersion(Version version)
    {
        this.version = version;
    }

    public boolean isSkip()
    {
        return skip;
    }

    public void setSkip(boolean skip)
    {
        this.skip = skip;
    }

    public SQLScript getScript()
    {
        return script;
    }

    public void setScript(SQLScript script)
    {
        this.script = script;
    }

    @Override
    public int compareTo(Patch o)
    {
        // version then index
        if (this.version.equals(o.version))
        {
            return this.index - o.index;
        }
        return this.version.compareTo(o.version);
    }
}
