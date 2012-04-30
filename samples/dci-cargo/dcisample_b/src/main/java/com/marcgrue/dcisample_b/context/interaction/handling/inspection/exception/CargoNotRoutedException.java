package com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception;

import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;

public class CargoNotRoutedException extends InspectionException
{
    public CargoNotRoutedException( HandlingEvent handlingEvent )
    {
        super( handlingEvent );
    }

    @Override
    public String getMessage()
    {
        return "\nCargo is NOT ROUTED while being handled!" + handlingEvent.print()
              + "MOCKUP REQUEST TO CARGO OWNER: Please re-route cargo '" + id + "' (now in " + city + ").";
    }
}