package com.intrbiz.data.db.compiler.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.data.db.compiler.meta.SQLUserDefined;

public class UserDefinedUtil
{
    public static String[] buildSQL(Class<?> enclosingClass, SQLUserDefined userDefined)
    {
        List<String> sql = new LinkedList<String>();
        // load resources
        for (String resource : userDefined.resources())
        {
            sql.add(fromResource(enclosingClass, resource));
        }
        // copy sql
        for (String value : userDefined.value())
        {
            sql.add(value);
        }
        return sql.toArray(new String[sql.size()]);
    }
    
    public static final String fromResource(Class<?> enclosingClass, String resourceName)
    {
        try (Reader r = new BufferedReader(new InputStreamReader(enclosingClass.getResourceAsStream(resourceName))))
        {
            StringBuilder sb = new StringBuilder();
            int l;
            char[] b = new char[1024];
            while ((l = r.read(b)) != -1)
            {
                sb.append(b, 0, l);
            }
            return sb.toString();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load the resource \"" + resourceName, e);
        }
    }
}
