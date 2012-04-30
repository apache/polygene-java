package com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception;

import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;

public class CargoArrivedException extends InspectionException
{
    public CargoArrivedException( HandlingEvent handlingEvent )
    {
        super( handlingEvent );
    }

    @Override
    public String getMessage()
    {
        msg = "Cargo '" + id + "' has arrived in destination " + location + ".";
        msg += "\nMOCKUP REQUEST TO CARGO OWNER: Please claim cargo '" + id + "' in " + city + ".";

        return msg;
    }
}