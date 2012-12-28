package org.qi4j.library.constraints.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.library.constraints.OneOfConstraint;

/**
 * Marks a property as being a string, non null, one of the specified strings.
 */
@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Constraints( OneOfConstraint.class )
public @interface OneOf
{
    public String[] value();
}