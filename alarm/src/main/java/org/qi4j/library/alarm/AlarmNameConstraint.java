package org.qi4j.library.alarm;

import org.qi4j.api.constraint.Constraint;

public class AlarmNameConstraint
    implements Constraint<AlarmName, String>
{
    @Override
    public boolean isValid( AlarmName annotation, String value )
    {
        int length = annotation.length();
        if( length < 1 )
        {
            length = 1;
        }
        boolean lengthConstraint = value.length() >= length;
        boolean whiteSpaceConstraint = value.trim().length() > 0;
        return lengthConstraint && whiteSpaceConstraint;
    }
}
