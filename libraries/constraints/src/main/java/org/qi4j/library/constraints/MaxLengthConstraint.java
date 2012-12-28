package org.qi4j.library.constraints;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.MaxLength;

/**
 * Implement @MaxLength constraint.
 */
public class MaxLengthConstraint
    implements Constraint<MaxLength, String>
{

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isValid( MaxLength annotation, String argument )
    {
        if( argument != null )
        {
            return argument.length() <= annotation.value();
        }

        return false;
    }

}
