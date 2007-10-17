package org.qi4j.library.framework.constraint;

import org.qi4j.api.Constraint;
import org.qi4j.library.framework.constraint.annotation.MinLength;

/**
 * TODO
 */
public class MinLengthConstraint
    implements Constraint<MinLength, String>
{
    public boolean isValid( MinLength annotation, String parameter ) throws NullPointerException
    {
        return parameter.length() >= annotation.value();
    }
}
