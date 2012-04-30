package com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception;

import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;

public class UnexpectedCarrierException extends InspectionException
{
    public UnexpectedCarrierException( HandlingEvent handlingEvent )
    {
        super( handlingEvent );
    }

    @Override
    public String getMessage()
    {
        return "\nCarrier of voyage " + voyage + " didn't expect a load in " + location;
    }
}