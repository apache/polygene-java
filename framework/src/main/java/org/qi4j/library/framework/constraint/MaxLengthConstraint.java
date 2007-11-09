package org.qi4j.library.framework.constraint;

import org.qi4j.ParameterConstraint;
import org.qi4j.library.framework.constraint.annotation.MaxLength;

/**
 * TODO
 */
public class MaxLengthConstraint
    implements ParameterConstraint<MaxLength, String>
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
