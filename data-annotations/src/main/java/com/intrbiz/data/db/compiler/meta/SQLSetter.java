package com.intrbiz.data.db.compiler.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SQLFunction()
public @interface SQLSetter {
    String name();
    Class<?> table();
    boolean upsert() default true;
    SQLVersion since();
}
