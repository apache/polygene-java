package org.qi4j.library.constraints;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.GreaterThan;

/**
 * Implement @GreaterThan constraint.
 */
public class GreaterThanConstraint
    implements Constraint<GreaterThan, Number>
{

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isValid( GreaterThan annotation, Number argument )
    {
        return argument.doubleValue() > annotation.value();
    }

}
