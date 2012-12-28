package org.qi4j.library.constraints;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.Contains;

/**
 * Implement @Contains constraint for String.
 */
public class ContainsConstraint
    implements Constraint<Contains, String>
{

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isValid( Contains annotation, String argument )
    {
        return argument.contains( annotation.value() );
    }

}
