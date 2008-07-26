package org.qi4j.library.framework.constraint;

import org.qi4j.composite.Constraint;
import org.qi4j.library.framework.constraint.annotation.Range;

/**
 * TODO
 */
public class RangeConstraint
    implements Constraint<Range, Number>
{
    public boolean isValid( Range range, Number argument )
    {
        return argument.doubleValue() <= range.max() && argument.doubleValue() >= range.min();
    }
}