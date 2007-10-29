package org.qi4j.library.framework.constraint;

import org.qi4j.annotation.Constraints;

/**
 * TODO
 */

@Constraints( { NotNullConstraint.class,
    MinLengthConstraint.class,
    MaxLengthConstraint.class,
    GreaterThanConstraint.class,
    LessThanConstraint.class,
    ContainsConstraint.class,
    MatchesConstraint.class } )
public interface DefaultConstraintsAbstractComposite
{
}
