package org.qi4j.library.framework.constraint;

import org.qi4j.ParameterConstraint;
import org.qi4j.library.framework.constraint.annotation.NotNull;

/**
 * TODO
 */
public class NotNullConstraint
    implements ParameterConstraint<NotNull, Object>
{
    public boolean isValid( NotNull annotation, Object object )
    {
        return object != null;
    }
}
