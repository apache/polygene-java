package org.qi4j.library.constraints;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.MinLength;

/**
 * Implement @MinLength constraint.
 */
public class MinLengthConstraint
    implements Constraint<MinLength, String>
{

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isValid( MinLength annotation, String parameter )
        throws NullPointerException
    {
        if( parameter != null )
        {
            return parameter.length() >= annotation.value();
        }

        return false;
    }

}
