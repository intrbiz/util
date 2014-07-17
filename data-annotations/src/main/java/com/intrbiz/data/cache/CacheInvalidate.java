package com.intrbiz.data.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A list of cache keys that are invalidated when the annotated method is invoked.
 * 
 * A cache key may end with a '*' to denote it removes all keys that start with it.
 * 
 * A cache key may use ${column_name} to interpolate the value of a column into the key.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheInvalidate
{
    String[] value();
}
