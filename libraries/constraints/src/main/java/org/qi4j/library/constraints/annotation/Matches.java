package org.qi4j.library.constraints.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.library.constraints.MatchesConstraint;

/**
 * Marks a property as being a string, non null, matching the specified regular expression.
 */
@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Constraints( MatchesConstraint.class )
public @interface Matches
{
    String value();
}
