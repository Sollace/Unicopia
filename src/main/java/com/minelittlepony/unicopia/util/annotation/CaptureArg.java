package com.minelittlepony.unicopia.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Informational annotation that indicates the argument it is
 * attached to is a parameter on the target method
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.SOURCE)
public @interface CaptureArg {

}
