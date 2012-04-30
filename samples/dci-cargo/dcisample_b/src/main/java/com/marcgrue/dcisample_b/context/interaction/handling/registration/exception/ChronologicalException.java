package com.marcgrue.dcisample_b.context.interaction.handling.registration.exception;

import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;

import java.util.Date;

/**
 * Thrown when trying to register a handling event with an unknown tracking id.
 */
public final class ChronologicalException extends CannotRegisterHandlingEventException
{
    Date lastCompletionTime;

    public ChronologicalException( ParsedHandlingEventData parsedHandlingEventData, Date lastCompletionTime )
    {
        super( parsedHandlingEventData );
        this.lastCompletionTime = lastCompletionTime;
    }

    @Override
    public String getMessage()
    {
        return "Completion time " + time + " is unexpectedly before last handling event completion.";
    }
}