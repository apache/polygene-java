package org.qi4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used by composites and mixins to declare what SideEffects
 * should apply to the type or specific method.
 * <p/>
 * If a method is invoked in a transactional scope, then the SideEffect will not be
 * executed before the transaction is committed. A rollback of a transaction will
 * cause the SideEffects from within that transaction to not be executed.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.TYPE, ElementType.METHOD } )
@Documented
public @interface SideEffects
{
    Class[] value();
}
