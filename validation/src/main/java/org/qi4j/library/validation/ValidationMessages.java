package org.qi4j.library.validation;

import java.util.Collection;

/**
 * TODO
 */
public interface ValidationMessages
{
    void addValidationMessage( ValidationMessage validationMessage );

    void removeValidationMessage( String message );

    Collection<ValidationMessage> getValidationMessages();

    void clearValidationMessages();
}
