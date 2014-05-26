package com.intrbiz.data.db.compiler.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Map a Java field to a SQL column
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SQLColumn
{
    /**
     * The order of the column in the table
     */
    int index();
    
    /**
     * The column name
     */
    String name();
    
    /**
     * The SQL type of the column
     */
    String type() default "";
    
    /**
     * The schema version that added this column
     */
    SQLVersion since();
    
    /**
     * Add a NOT NULL constraint to this column?
     */
    boolean notNull() default false;
    
    /**
     * Apply the given DBTypeAdapter to this field
     */
    Class<?> adapter() default NullAdapter.class;
    
    public static class NullAdapter {}
}
