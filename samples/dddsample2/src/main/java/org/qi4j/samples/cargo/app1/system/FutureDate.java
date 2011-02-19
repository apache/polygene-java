package org.qi4j.samples.cargo.app1.system;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;

/**
 * Constraint annotation to validate that only Uppcase characters are used.
 */
@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.METHOD } )
@Documented
@Constraints( FutureDateConstraint.class )
public @interface FutureDate
{
}