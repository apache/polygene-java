package org.qi4j.samples.cargo.app1.model.handling;

public class UnknownLocationException extends CannotCreateHandlingEventException
{

    private final String unlocode;

    public UnknownLocationException( final String unlocode )
    {
        this.unlocode = unlocode;
    }

    @Override
    public String getMessage()
    {
        return "No location with UN locode " + unlocode + " exists in the system";
    }
}
