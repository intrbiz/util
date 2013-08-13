package com.intrbiz.util.compiler.util;

import java.lang.reflect.Field;

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
    
    public static String escapeString(String str)
    {
        return str.replace("\"", "\\\"");
    }
    
    public static String lcFirst(String str)
    {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
    
    public static String ucFirst(String str)
    {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    public static String getterName(Field field)
    {
        if (boolean.class == field.getType() || Boolean.class == field.getType()) return "is" + ucFirst(field.getName());    
        return "get" + ucFirst(field.getName());
    }
    
    public static String setterName(Field field)
    {
        return "set" + ucFirst(field.getName());
    }
}
