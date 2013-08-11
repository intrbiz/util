package com.intrbiz.data.db.compiler.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SQLForeignKey {
    String name() default "";
    String[] columns() default {};
    Class<?> references();
    String[] on();
    Action onUpdate() default Action.NO_ACTION;
    Action onDelete() default Action.NO_ACTION;
    Deferable deferable() default Deferable.INITIALLY_DEFERRED;
}