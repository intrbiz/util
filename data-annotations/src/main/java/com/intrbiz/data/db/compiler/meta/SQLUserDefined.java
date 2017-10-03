package com.intrbiz.data.db.compiler.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rather then generate the database entity, use the entity defined here.
 * 
 * Each <code>resource()</code> will be looked up relative to the enclosing 
 * class and executed as a statement. Followed by each statement provided by
 * <code>value()</code>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SQLUserDefined {
    String dialect() default "SQL";
    String[] resources() default { };
    String[] value() default {};
}
