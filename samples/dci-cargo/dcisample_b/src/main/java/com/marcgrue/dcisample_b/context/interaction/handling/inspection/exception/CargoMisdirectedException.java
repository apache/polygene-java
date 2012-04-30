package com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception;

import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;

/**
 * CargoMisdirectedException
 *
 * This would have to set off notifying the cargo owner and request a re-route.
 */
public class CargoMisdirectedException extends InspectionException
{
    private Itinerary itinerary;
    public CargoMisdirectedException( HandlingEvent handlingEvent, String msg )
    {
        super( handlingEvent, msg );
    }

    public CargoMisdirectedException( HandlingEvent handlingEvent, Itinerary itinerary, String msg )
    {
        super( handlingEvent, msg );
        this.itinerary = itinerary;
    }

    @Override
    public String getMessage()
    {
        String itineraryString = "";
        if(itinerary != null)
            itineraryString = itinerary.print();

        return "\nCargo is MISDIRECTED! " + msg + "\n" + handlingEvent.print() + itineraryString
              + "MOCKUP REQUEST TO CARGO OWNER: Please re-route misdirected cargo '" + id + "' (now in " + city + ").";
    }
}