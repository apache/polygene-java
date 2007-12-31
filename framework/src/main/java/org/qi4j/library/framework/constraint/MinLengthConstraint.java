package org.qi4j.library.framework.constraint;

import org.qi4j.composite.Constraint;
import org.qi4j.library.framework.constraint.annotation.MinLength;

/**
 * TODO
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
