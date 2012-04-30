package com.marcgrue.dcisample_b.context.interaction.handling.registration.exception;

import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;

/**
 * Thrown when trying to register a handling event after cargo has been claimed.
 */
public final class AlreadyClaimedException extends CannotRegisterHandlingEventException
{
    public AlreadyClaimedException( ParsedHandlingEventData parsedHandlingEventData )
    {
        super( parsedHandlingEventData );
    }

    @Override
    public String getMessage()
    {
        return type + " handling event can't be registered after cargo has been claimed.";
    }
}
