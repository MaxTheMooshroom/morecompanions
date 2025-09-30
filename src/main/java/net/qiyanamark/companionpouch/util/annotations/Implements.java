package net.qiyanamark.companionpouch.util.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Implements {
    Class<?> value();
    Class<?>[] introducedBy() default {};
}
