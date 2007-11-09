package org.qi4j.library.framework.constraint;

import org.qi4j.ParameterConstraint;
import org.qi4j.library.framework.constraint.annotation.LessThan;

/**
 * TODO
 */
public class LessThanConstraint
    implements ParameterConstraint<LessThan, Number>
{
    public boolean isValid( LessThan annotation, Number argument )
    {
        return argument.doubleValue() < annotation.value();
    }
}
