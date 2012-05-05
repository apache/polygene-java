package com.marcgrue.dcisample_b.context.interaction.handling.registration.exception;

import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;

/**
 * Thrown when trying to register a handling event without a required voyage
 */
public final class MissingVoyageNumberException extends CannotRegisterHandlingEventException
{
    public MissingVoyageNumberException( ParsedHandlingEventData parsedHandlingEventData )
    {
        super( parsedHandlingEventData );
    }

    @Override
    public String getMessage()
    {
        msg = "Unsuccessful handling event registration for cargo '" + id + "' (handling event '" + type + "' in '" + unloc + "')." +
              "\nMissing voyage number. Handling event " + type + " requires a voyage.";

        msg+= "\nMOCKUP NOTIFICATION TO HANDLING AUTHORITY: Please check submitted invalid handling event data:";
        msg += getParsedHandlingEventData();

        return msg;
    }
}
