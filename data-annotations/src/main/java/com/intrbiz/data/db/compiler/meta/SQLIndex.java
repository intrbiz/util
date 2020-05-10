package com.intrbiz.data.db.compiler.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface SQLIndex {
    String name() default "";
    String using() default "btree";
    String[] columns() default {};
    String expression() default "";
    /**
     * The schema version that added this column
     */
    SQLVersion since();
}
