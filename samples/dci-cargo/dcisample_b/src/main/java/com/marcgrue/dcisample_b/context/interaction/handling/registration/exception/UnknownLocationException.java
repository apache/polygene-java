package com.marcgrue.dcisample_b.context.interaction.handling.registration.exception;

import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;

/**
 * Thrown when trying to register a handling event with an unknown location.
 */
public final class UnknownLocationException extends CannotRegisterHandlingEventException
{
    public UnknownLocationException( ParsedHandlingEventData parsedHandlingEventData )
    {
        super( parsedHandlingEventData );
    }

    @Override
    public String getMessage()
    {
        return "Found no location with UN locode '" + unloc + "'.";
    }
}
