package org.qi4j.library.constraints;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.MinLength;

/**
 * JAVADOC
 */
public class MinLengthConstraint
    implements Constraint<MinLength, String>
{
    public boolean isValid( MinLength annotation, String parameter ) throws NullPointerException
    {
        if( parameter != null )
        {
            return parameter.length() >= annotation.value();
        }

        return false;
    }
}
