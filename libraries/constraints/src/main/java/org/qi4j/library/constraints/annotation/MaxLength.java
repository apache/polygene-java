package org.qi4j.library.constraints.annotation;

import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.library.constraints.MaxLengthConstraint;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Constraints( MaxLengthConstraint.class )
public @interface MaxLength
{
    int value();
}
