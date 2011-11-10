package org.qi4j.library.constraints.annotation;

import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.library.constraints.LessThanConstraint;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Constraints( LessThanConstraint.class )
public @interface LessThan
{
    double value();
}
