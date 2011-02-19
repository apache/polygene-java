package org.qi4j.samples.dddsample.spring.ui.tracking;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validator for {@link se.citerus.dddsample.ui.command.TrackCommand}s.
 */
public final class TrackCommandValidator
    implements Validator
{
    public final boolean supports( final Class clazz )
    {
        return TrackCommand.class.isAssignableFrom( clazz );
    }

    public final void validate( final Object object, final Errors errors )
    {
        ValidationUtils.rejectIfEmptyOrWhitespace( errors, "trackingId", "error.required", "Required" );
    }
}