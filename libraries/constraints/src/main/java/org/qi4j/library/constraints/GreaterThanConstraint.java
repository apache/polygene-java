package org.qi4j.library.constraints;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.GreaterThan;

/**
 * JAVADOC
 */
public class GreaterThanConstraint
    implements Constraint<GreaterThan, Number>
{
    public boolean isValid( GreaterThan annotation, Number argument )
    {
        return argument.doubleValue() > annotation.value();
    }
}
