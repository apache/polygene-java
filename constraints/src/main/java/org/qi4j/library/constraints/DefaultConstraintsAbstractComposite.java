package org.qi4j.library.constraints;

import org.qi4j.composite.Constraints;

/**
 * TODO
 */

@Constraints( { NotNullConstraint.class,
    MinLengthConstraint.class,
    MaxLengthConstraint.class,
    GreaterThanConstraint.class,
    LessThanConstraint.class,
    ContainsConstraint.class,
    InstanceOfConstraint.class,
    MatchesConstraint.class } )
public interface DefaultConstraintsAbstractComposite
{
}
