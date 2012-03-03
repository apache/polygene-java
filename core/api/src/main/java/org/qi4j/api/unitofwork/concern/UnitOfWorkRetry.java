package org.qi4j.api.unitofwork.concern;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation describes the retries that should occur in case of {@link org.qi4j.api.unitofwork.ConcurrentEntityModificationException}
 * occurs.
 */
@Retention( RUNTIME )
@Target( METHOD )
@Inherited
@Documented
public @interface UnitOfWorkRetry
{
    int retries() default 1;

    long initialDelay() default 0;

    long delayFactory() default 10;
}
