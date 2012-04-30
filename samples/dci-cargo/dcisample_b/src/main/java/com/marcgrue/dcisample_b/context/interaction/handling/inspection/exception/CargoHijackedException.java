package com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception;

import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;

/**
 * The easter egg.
 */
public class CargoHijackedException extends InspectionException
{
    public CargoHijackedException( HandlingEvent handlingEvent )
    {
        super( handlingEvent );
    }

    @Override
    public String getMessage()
    {
        msg = "Cargo '" + id + "' was hijacked.";
        msg += "\nMOCKUP MESSAGE TO CARGO OWNER: We're sorry to inform you that your cargo '" + id
              + "' was hijacked. Please contact your insurance company.";

        return msg;
    }
}