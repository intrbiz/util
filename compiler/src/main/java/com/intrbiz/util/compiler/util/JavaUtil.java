package com.intrbiz.util.compiler.util;

public class JavaUtil
{
    public static boolean isJavaPrimitive(String type)
    {
        return "int".equals(type) ||
               "long".equals(type) ||
               "boolean".equals(type) ||
               "float".equals(type) ||
               "double".equals(type) ||
               "short".equals(type) ||
               "byte".equals(type) ||
               "character".equals(type);
    }
}
