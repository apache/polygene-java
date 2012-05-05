package com.marcgrue.dcisample_b.context.interaction.booking.exception;

import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;

/**
 * Javadoc
 */
public class UnsatisfyingRouteException extends Exception
{
    private RouteSpecification routeSpec;
    private Itinerary itinerary;

    public UnsatisfyingRouteException( RouteSpecification routeSpec, Itinerary itinerary )
    {
        this.routeSpec = routeSpec;
        this.itinerary = itinerary;
    }
    @Override
    public String getMessage()
    {
//        return "Route specification was not satisfied with itinerary.";

        // When testing:
        return "Route specification was not satisfied with itinerary:\n" + routeSpec.print() + "\n" + itinerary.print();
    }
}