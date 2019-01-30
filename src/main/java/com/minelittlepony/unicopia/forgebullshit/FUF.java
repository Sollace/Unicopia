package com.minelittlepony.unicopia.forgebullshit;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Workaround because forge freaks out at the nearest mention
 * of the possibility of a client class in a server environment
 * even when the logic in the code means it would never be executed.
 *
 * #FuckUForge
 */
@Documented
@Retention(SOURCE)
@Target({METHOD, FIELD, TYPE, CONSTRUCTOR, PACKAGE})
public @interface FUF {
    String reason() default "";
}
