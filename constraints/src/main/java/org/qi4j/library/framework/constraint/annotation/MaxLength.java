package org.qi4j.library.framework.constraint.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.qi4j.composite.ConstraintDeclaration;
import org.qi4j.composite.Constraints;
import org.qi4j.library.framework.constraint.MaxLengthConstraint;

@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Constraints( MaxLengthConstraint.class )
public @interface MaxLength
{
    int value();
}
