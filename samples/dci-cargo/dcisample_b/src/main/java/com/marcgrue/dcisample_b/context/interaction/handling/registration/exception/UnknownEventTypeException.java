package com.marcgrue.dcisample_b.context.interaction.handling.registration.exception;

import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType;

import java.util.Arrays;

/**
 * Thrown when trying to register a handling event with an unknown handling event type.
 */
public final class UnknownEventTypeException extends CannotRegisterHandlingEventException
{
    public UnknownEventTypeException( ParsedHandlingEventData parsedHandlingEventData )
    {
        super( parsedHandlingEventData );
    }

    @Override
    public String getMessage()
    {
        return "'" + type + "' is not a valid handling event type. Valid types are: "
              + Arrays.toString( HandlingEventType.values() );
    }
}