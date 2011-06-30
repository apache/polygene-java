package org.qi4j.samples.cargo.app1.system;

import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;

import java.lang.annotation.*;


/**
 * Constraint annotation to validate that only Uppcase characters are used.
 */
@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.METHOD } )
@Documented
@Constraints( UnLocodeConstraint.class )
public @interface UnLocode {
}