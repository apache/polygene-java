package org.qi4j.library.constraints;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.LessThan;

/**
 * JAVADOC
 */
public class LessThanConstraint
    implements Constraint<LessThan, Number>
{
    public boolean isValid( LessThan annotation, Number argument )
    {
        return argument.doubleValue() < annotation.value();
    }
}
