package org.qi4j.library.constraints.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.library.constraints.GreaterThanConstraint;

/**
 * Marks a property as being a number, non null, greater than the specified value.
 */
@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Constraints( GreaterThanConstraint.class )
public @interface GreaterThan
{
    double value();
}
