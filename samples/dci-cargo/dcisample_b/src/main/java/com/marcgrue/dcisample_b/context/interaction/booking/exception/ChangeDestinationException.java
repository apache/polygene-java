package com.marcgrue.dcisample_b.context.interaction.booking.exception;

import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;

/**
 * Javadoc
 */
public class ChangeDestinationException extends Exception
{

//    private RouteSpecification routeSpec;
//    private Itinerary itinerary;
    protected String msg = "";


    public ChangeDestinationException( String s, RouteSpecification routeSpec, Itinerary itinerary )
    {
        msg = s;
//        this.routeSpec = routeSpec;
//        this.itinerary = itinerary;
    }

    @Override
    public String getMessage()
    {
        return msg;
//        return "Couldn't change destination of cargo: " + msg;
    }







    public ChangeDestinationException( Throwable e )
    {
        super(e);
    }
    public ChangeDestinationException()
    {
        super();
    }

    public ChangeDestinationException( String s )
    {
        msg = s;
    }
}