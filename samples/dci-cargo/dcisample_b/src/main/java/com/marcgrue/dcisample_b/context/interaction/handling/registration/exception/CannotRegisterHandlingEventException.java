package com.marcgrue.dcisample_b.context.interaction.handling.registration.exception;

import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * If a {@link HandlingEvent} can't be created from a given set of parameters.
 *
 * It is a checked exception because it's not a programming error, but rather a
 * special case that the application is built to handle. It can occur during normal
 * program execution.
 */
public class CannotRegisterHandlingEventException extends Exception
{
    private ParsedHandlingEventData parsedHandlingEventData;

    protected String msg, id, time, type, unloc;
    protected String voy = "";

    public CannotRegisterHandlingEventException( ParsedHandlingEventData parsedHandlingEventData )
    {
        super();
        this.parsedHandlingEventData = parsedHandlingEventData;

        time = parseDate( parsedHandlingEventData.completionTime().get() );
        id = parse( parsedHandlingEventData.trackingIdString().get() );
        type = parse( parsedHandlingEventData.handlingEventType().get().name() );
        unloc = parse( parsedHandlingEventData.unLocodeString().get() );
        voy = parse( parsedHandlingEventData.voyageNumberString().get() );
    }

    public ParsedHandlingEventData getParsedHandlingEventData()
    {
        return parsedHandlingEventData;
    }
    private String parse( String str )
    {
        return str == null ? "null" : str;
    }

    private String parseDate( Date date )
    {
        return date == null ? "null" : new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( date );
    }
}