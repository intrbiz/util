package com.intrbiz.data.db.compiler.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * A method which will return a SQLScript which should be 
 * used as part of the install or update process
 * </p>
 * <p>
 * The method must be static
 * </p>
 * <p>
 * For example:
 * </p>
 * <pre>
 *   @SQLScript
 *   public static SQLScript insertDefaultUser() {
 *     return new SQLScript("INSERT INTO users (username) VALUES ('admin')");
 *   }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SQLPatch {
    String name();
    int index();
    ScriptType type();              // install or upgrade
    SQLVersion version();              // target version
    boolean skip() default false;   // can this patch be skipped if the upgrade is jumping many versions
}
