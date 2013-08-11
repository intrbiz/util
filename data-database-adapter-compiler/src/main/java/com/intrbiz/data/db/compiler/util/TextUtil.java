package com.intrbiz.data.db.compiler.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil
{
    public static String lcFirst(String str)
    {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
    
    public static String ucFirst(String str)
    {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * Convert a string in camelCase form to under_score form.
     * Eg: fullName -> full_name
     */
    public static String camelCaseToUnderscores(String name)
    {
        StringBuilder sb = new StringBuilder();
        Pattern p = Pattern.compile("[A-Z]+");
        Matcher m = p.matcher(name);
        int last = 0;
        while (m.find())
        {
            if (m.start() > 0)
            {
                sb.append(name.substring(last, m.start()));
                sb.append("_");
            }
            sb.append(name.substring(m.start(), m.end()).toLowerCase());
            last = m.end();
        }
        if (last < name.length()) sb.append(name.substring(last, name.length()));
        return sb.toString();
    }
}
