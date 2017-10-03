package com.intrbiz.data.db.compiler.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Don't do any funky auto generating shit,
 * just exeucte the user defined SQL and run
 * the user defined method
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SQLFunction()
public @interface SQLCustom
{
    SQLVersion since();
    SQLUserDefined[] userDefined() default {};
}
