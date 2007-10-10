package org.qi4j.library.general.model.modifiers;

import java.util.List;
import org.qi4j.api.annotation.scope.AssertionFor;
import org.qi4j.library.general.model.Validatable;
import org.qi4j.library.general.model.ValidationException;
import org.qi4j.library.general.model.ValidationMessage;
import org.qi4j.library.general.model.Validator;

/**
 * Base class for validation assertions.
 */
public class AbstractValidatableAssertion
    implements Validatable
{
    protected @AssertionFor Validatable next;

    public List<ValidationMessage> isValid()
    {
        List<ValidationMessage> messages = next.isValid();
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
