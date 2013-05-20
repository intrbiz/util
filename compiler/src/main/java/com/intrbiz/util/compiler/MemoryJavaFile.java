package com.intrbiz.util.compiler;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class MemoryJavaFile extends SimpleJavaFileObject
{
    private final String code;

    public MemoryJavaFile(String name, String code)
    {
        super(URI.create("mem:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
    {
        return code;
    }
}
