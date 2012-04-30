package com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception;

/**
 * Truly unexpected errors.
 */
public class InspectionFailedException extends InspectionException
{
    public InspectionFailedException( String s )
    {
        super( s );
    }

    @Override
    public String getMessage()
    {
        return "INTERNAL ERROR: " + super.getMessage();
    }
}