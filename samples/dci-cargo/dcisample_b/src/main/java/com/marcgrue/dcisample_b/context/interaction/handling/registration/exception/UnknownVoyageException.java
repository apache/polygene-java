package com.marcgrue.dcisample_b.context.interaction.handling.registration.exception;

import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;

/**
 * Thrown when trying to register an event with an unknown voyage number.
 */
public final class UnknownVoyageException extends CannotRegisterHandlingEventException
{
    public UnknownVoyageException( ParsedHandlingEventData parsedHandlingEventData )
    {
        super( parsedHandlingEventData );
    }

    @Override
    public String getMessage()
    {
        return "Found no voyage with voyage number '" + voy + "'.";
    }
}
