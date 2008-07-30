package org.qi4j.library.constraints;

import org.qi4j.composite.Constraint;
import org.qi4j.library.constraints.annotation.LessThan;

/**
 * TODO
 */
public class LessThanConstraint
    implements Constraint<LessThan, Number>
{
    public boolean isValid( LessThan annotation, Number argument )
    {
        return argument.doubleValue() < annotation.value();
    }
}
