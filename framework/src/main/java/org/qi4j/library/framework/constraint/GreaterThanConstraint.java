package org.qi4j.library.framework.constraint;

import org.qi4j.composite.Constraint;
import org.qi4j.library.framework.constraint.annotation.GreaterThan;

/**
 * TODO
 */
public class GreaterThanConstraint
    implements Constraint<GreaterThan, Number>
{
    public boolean isValid( GreaterThan annotation, Number argument )
    {
        return argument.doubleValue() < annotation.value();
    }
}
