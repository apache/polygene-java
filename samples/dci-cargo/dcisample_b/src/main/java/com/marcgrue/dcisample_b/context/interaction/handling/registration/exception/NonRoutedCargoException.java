package com.marcgrue.dcisample_b.context.interaction.handling.registration.exception;

import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;

/**
 * Thrown when trying to register a handling event for an nonRouted cargo.
 */
public class NonRoutedCargoException extends CannotRegisterHandlingEventException
{
    public NonRoutedCargoException( ParsedHandlingEventData parsedHandlingEventData )
    {
        super( parsedHandlingEventData );
    }

    @Override
    public String getMessage()
    {
        msg = "\nUnsuccessful handling event registration for cargo '" + id +
              "' (handling event '" + type + "' in '" + unloc + "'). Cargo is not routed!";

        msg += "\nMOCKUP NOTIFICATION TO HANDLING AUTHORITY: Please check submitted invalid handling event data:";
        msg += getParsedHandlingEventData();

        return msg;
    }
}