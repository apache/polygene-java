package org.qi4j.manual.recipes.createConstraint;

import java.util.Collection;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.property.Property;

// START SNIPPET: property
public abstract class PhoneNumberParameterViolationConcern extends ConcernOf<HasPhoneNumber>
    implements HasPhoneNumber
{
    @Concerns( CheckViolation.class )
    public abstract Property<String> phoneNumber();

    private abstract class CheckViolation extends ConcernOf<Property<String>>
        implements Property<String>
    {
        public void set( String number )
        {
            try
            {
                next.set( number );
            }
            catch( ConstraintViolationException e )
            {
                Collection<ConstraintViolation> violations = e.constraintViolations();
                report( violations );
            }
        }

// END SNIPPET: property

// START SNIPPET: property
        private void report( Collection<ConstraintViolation> violations )
        {
        }
    }
}
// END SNIPPET: property
