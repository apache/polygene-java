package org.qi4j.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.qi4j.api.Constraint;

/**
 * This annotation is used by composites and mixins to declare what Constraints
 * can be applied in the Composite.
 * <p/>
 * Constraints implement the {@see ConstraintDeclaration} interface
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.TYPE } )
@Documented
public @interface Constraints
{
    Class<? extends Constraint>[] value();
}
