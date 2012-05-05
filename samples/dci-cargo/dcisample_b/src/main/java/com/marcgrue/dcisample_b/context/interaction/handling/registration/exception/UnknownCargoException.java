package com.marcgrue.dcisample_b.context.interaction.handling.registration.exception;

import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;

/**
 * Thrown when trying to register a handling event with an unknown tracking id.
 */
public final class UnknownCargoException extends CannotRegisterHandlingEventException
{
    public UnknownCargoException( ParsedHandlingEventData parsedHandlingEventData )
    {
        super( parsedHandlingEventData );
    }

    @Override
    public String getMessage()
    {
        return "Found no cargo with tracking id '" + id + "'.";
    }
}