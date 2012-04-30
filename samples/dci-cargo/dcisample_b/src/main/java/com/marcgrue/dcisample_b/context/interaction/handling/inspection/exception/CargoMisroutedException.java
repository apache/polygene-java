package com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception;

import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;

/**
 * CargoMisroutedException
 *
 * This would have to set off notifying the cargo owner and request a re-route.
 */
public class CargoMisroutedException extends InspectionException
{
    private RouteSpecification routeSpecification;
    private Itinerary itinerary;

    public CargoMisroutedException( HandlingEvent handlingEvent, RouteSpecification routeSpecification, Itinerary itinerary )
    {
        super( handlingEvent );
        this.routeSpecification = routeSpecification;
        this.itinerary = itinerary;
    }

    @Override
    public String getMessage()
    {
        return "\nCargo is MISROUTED! Route specification is not satisfied with itinerary:\n"
              + routeSpecification.print() + itinerary.print()
              + "MOCKUP REQUEST TO CARGO OWNER: Please re-route misrouted cargo '" + id + "' (now in " + city + ").";
    }
}