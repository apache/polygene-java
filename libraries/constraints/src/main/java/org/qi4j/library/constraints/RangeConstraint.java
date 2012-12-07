package org.qi4j.library.constraints;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.Range;

/**
 * Implement @Range constraint.
 */
public class RangeConstraint
    implements Constraint<Range, Number>
{

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isValid( Range range, Number argument )
    {
        return argument.doubleValue() <= range.max() && argument.doubleValue() >= range.min();
    }

}