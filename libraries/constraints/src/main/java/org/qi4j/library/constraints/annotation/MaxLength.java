package org.qi4j.library.constraints.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.library.constraints.MaxLengthConstraint;

/**
 * Marks a property as being a string, non null, of specified maximum length.
 */
@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Constraints( MaxLengthConstraint.class )
public @interface MaxLength
{
    int value();
}
