package org.qi4j.library.validation;

import java.util.List;
import org.qi4j.api.concern.ConcernOf;

/**
 * Base class for validation concerns.
 */
public class AbstractValidatableConcern extends ConcernOf<Validatable>
    implements Validatable
{
    public List<ValidationMessage> validate()
    {
        List<ValidationMessage> messages = next.validate();
        Validator validator = new Validator( messages, getResourceBundle() );
        isValid( validator );
        return messages;
    }

    public void checkValid() throws ValidationException
    {
        next.checkValid();
    }

    /**
     * Override this method to do your own validations
     *
     * @param validator used to simplify checks
     */
    protected void isValid( Validator validator )
    {
    }

    protected String getResourceBundle()
    {
        return this.getClass().getPackage().getName() + ".package";
    }
}
