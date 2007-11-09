package org.qi4j.library.framework.constraint;

import org.qi4j.ParameterConstraint;
import org.qi4j.library.framework.constraint.annotation.Contains;

/**
 * TODO
 */
public class ContainsConstraint
    implements ParameterConstraint<Contains, String>
{
    public boolean isValid( Contains annotation, String argument )
    {
        return argument.contains( annotation.value() );
    }
}
