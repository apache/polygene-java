package org.qi4j.library.constraints;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.Contains;

/**
 * JAVADOC
 */
public class ContainsConstraint
    implements Constraint<Contains, String>
{
    public boolean isValid( Contains annotation, String argument )
    {
        return argument.contains( annotation.value() );
    }
}
