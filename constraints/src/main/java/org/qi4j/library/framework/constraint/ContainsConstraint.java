package org.qi4j.library.framework.constraint;

import org.qi4j.composite.Constraint;
import org.qi4j.library.framework.constraint.annotation.Contains;

/**
 * TODO
 */
public class ContainsConstraint
    implements Constraint<Contains, String>
{
    public boolean isValid( Contains annotation, String argument )
    {
        return argument.contains( annotation.value() );
    }
}
