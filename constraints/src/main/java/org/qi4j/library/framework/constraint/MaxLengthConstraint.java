package org.qi4j.library.framework.constraint;

import org.qi4j.composite.Constraint;
import org.qi4j.library.framework.constraint.annotation.MaxLength;

/**
 * TODO
 */
public class MaxLengthConstraint
    implements Constraint<MaxLength, String>
{
    public boolean isValid( MaxLength annotation, String argument )
    {
        if( argument != null )
        {
            return argument.length() <= annotation.value();
        }

        return false;
    }
}
