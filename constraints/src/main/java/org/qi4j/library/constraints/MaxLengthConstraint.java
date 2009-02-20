package org.qi4j.library.constraints;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.MaxLength;

/**
 * JAVADOC
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
