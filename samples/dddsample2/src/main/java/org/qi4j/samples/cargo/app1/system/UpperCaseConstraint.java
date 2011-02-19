package org.qi4j.samples.cargo.app1.system;

import org.qi4j.api.constraint.Constraint;

/**
 *
 */
public class UpperCaseConstraint
    implements Constraint<UpperCaseOnly, String>
{

    public boolean isValid( final UpperCaseOnly upperCaseOnly, final String value )
    {
        return value.equals( value.toUpperCase() );
    }
}