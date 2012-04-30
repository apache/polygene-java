package com.marcgrue.dcisample_b.context.interaction.handling.registration.exception;

import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;

/**
 * Thrown when trying to register a receive/in customs/claim handling event twice.
 */
public final class DuplicateEventException extends CannotRegisterHandlingEventException
{
    public DuplicateEventException( ParsedHandlingEventData parsedHandlingEventData )
    {
        super( parsedHandlingEventData );
    }

    @Override
    public String getMessage()
    {
        if (type.equals( "RECEIVE" ))
            return "Cargo can't be received more than once.";
        else if (type.equals( "CUSTOMS" ))
            return "Cargo can't be in customs more than once.";
        else if (type.equals( "CLAIM" ))
            return "Cargo can't be claimed more than once.";
        else
            return "INTERNAL ERROR: Unexpected handling event type for this exception";
    }
}
