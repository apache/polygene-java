package org.qi4j.library.constraints;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.LessThan;

/**
 * Implement @LessThan constraint.
 */
public class LessThanConstraint
    implements Constraint<LessThan, Number>
{

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isValid( LessThan annotation, Number argument )
    {
        return argument.doubleValue() < annotation.value();
    }

}
