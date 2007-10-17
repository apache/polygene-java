package org.qi4j.library.framework.constraint;

import org.qi4j.api.Constraint;
import org.qi4j.library.framework.constraint.annotation.NotNull;

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
