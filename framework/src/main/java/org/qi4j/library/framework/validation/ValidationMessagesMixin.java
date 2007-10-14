package org.qi4j.library.framework.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 */
public class ValidationMessagesMixin
    implements ValidationMessages
{
    Map<String, ValidationMessage> messages = new HashMap<String, ValidationMessage>();

    public void addValidationMessage( ValidationMessage validationMessage )
    {
        messages.put( validationMessage.getResourceKey(), validationMessage );
    }

    public void removeValidationMessage( String message )
    {
        messages.remove( message );
    }

    public Collection<ValidationMessage> getValidationMessages()
    {
        return messages.values();
    }

    public void clearValidationMessages()
    {
        messages.clear();
    }
}
