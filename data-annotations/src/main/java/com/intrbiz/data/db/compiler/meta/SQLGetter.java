package com.intrbiz.data.db.compiler.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SQLFunction()
public @interface SQLGetter
{
    String name();
    Class<?> table();
    SQLOrder[] orderBy() default {};
    SQLQuery[] query() default {};
    SQLVersion since();
    SQLUserDefined[] userDefined() default {};
}
