package org.qi4j.library.constraints;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.OneOf;

/**
 * Implement @OneOf constraint.
 */
public class OneOfConstraint
    implements Constraint<OneOf, String>
{

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isValid( OneOf oneOf, String value )
    {
        for( int i = 0; i < oneOf.value().length; i++ )
        {
            String possibleValue = oneOf.value()[ i];
            if( possibleValue.equals( value ) )
            {
                return true;
            }
        }
        return false;
    }

}