package org.qi4j.library.framework.constraint.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.qi4j.composite.ConstraintDeclaration;
import org.qi4j.composite.Constraints;
import org.qi4j.library.framework.constraint.LessThanConstraint;

@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Constraints( LessThanConstraint.class )
public @interface LessThan
{
    double value();
}
