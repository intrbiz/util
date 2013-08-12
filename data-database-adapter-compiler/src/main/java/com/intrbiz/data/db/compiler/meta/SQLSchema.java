package com.intrbiz.data.db.compiler.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SQLSchema {
    String name();
    SQLVersion version();
    Class<?>[] tables();
    Class<?>[] patches() default {};
}
