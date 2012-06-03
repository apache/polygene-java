package org.qi4j.manual.recipes.createConstraint;

import org.qi4j.api.constraint.Constraint;

// START SNIPPET: constraint
public class PhoneNumberConstraint
        implements Constraint<PhoneNumber, String>
{
    public boolean isValid( PhoneNumber annotation, String number )
    {
        boolean validPhoneNumber = true; // check phone number format...
        return validPhoneNumber;  // return true if valid phone number.
    }
}
// END SNIPPET: constraint
