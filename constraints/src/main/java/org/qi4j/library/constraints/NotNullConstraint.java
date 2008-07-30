package org.qi4j.library.constraints;

import org.qi4j.composite.Constraint;
import org.qi4j.library.constraints.annotation.NotNull;

/**
 * TODO
 */
public class NotNullConstraint
    implements Constraint<NotNull, Object>
{
    public boolean isValid( NotNull annotation, Object object )
    {
        return object != null;
    }
}
