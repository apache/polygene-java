package org.qi4j.samples.dddsample.domain.service;

import org.qi4j.samples.dddsample.domain.model.location.UnLocode;

public class UnknownLocationException
    extends Exception
{

    private final UnLocode unlocode;

    public UnknownLocationException( final UnLocode unlocode )
    {
        this.unlocode = unlocode;
    }

    @Override
    public String getMessage()
    {
        return "No location with UN locode " + unlocode.idString() + " exists in the system";
    }
}