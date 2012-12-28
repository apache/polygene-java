package org.qi4j.library.constraints.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.library.constraints.ContainsConstraint;

/**
 * Marks a property as being a string, non null, that contains the specified value.
 */
@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Constraints( ContainsConstraint.class )
public @interface Contains
{
    String value();
}
