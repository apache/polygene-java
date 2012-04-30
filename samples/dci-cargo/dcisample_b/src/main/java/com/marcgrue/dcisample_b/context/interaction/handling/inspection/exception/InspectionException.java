package com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception;

import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;

import java.text.SimpleDateFormat;

/**
 * Base exception for all variations of inspection.
 */
public class InspectionException extends Exception
{
    protected HandlingEvent handlingEvent;
    protected String id, registered, completion, type, city, unloc, location;
    protected String voyage = "";
    protected String msg = "";

    public InspectionException( HandlingEvent handlingEvent )
    {
        super();
        this.handlingEvent = handlingEvent;

        SimpleDateFormat date = new SimpleDateFormat( "yyyy-MM-dd" );
        id = handlingEvent.trackingId().get().id().get();
        registered = date.format( handlingEvent.registrationTime().get() );
        completion = date.format( handlingEvent.completionTime().get() );
        type = handlingEvent.handlingEventType().get().name();
        city = handlingEvent.location().get().name().get();
        unloc = handlingEvent.location().get().getCode();
        location = handlingEvent.location().get().getString();

        if (handlingEvent.voyage().get() != null)
            voyage = handlingEvent.voyage().get().voyageNumber().get().number().get();
    }

    public InspectionException( HandlingEvent handlingEvent, String message )
    {
        this( handlingEvent );
        msg = message;
    }

    public InspectionException( String s )
    {
        super( s );
    }
}