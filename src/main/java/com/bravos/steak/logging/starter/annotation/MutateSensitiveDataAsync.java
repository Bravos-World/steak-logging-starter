package com.bravos.steak.logging.starter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that sensitive data mutation should be handled asynchronously. Only can use on return type {@link org.reactivestreams.Publisher}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MutateSensitiveDataAsync {

}
