package org.qi4j.samples.dddsample.domain.service;

import org.qi4j.samples.dddsample.domain.model.cargo.TrackingId;

/**
 * Thrown when trying to register an event with an unknown tracking id.
 */
public class UnknownTrackingIdException
    extends Exception
{

    private final TrackingId trackingId;

    public UnknownTrackingIdException( final TrackingId trackingId )
    {
        this.trackingId = trackingId;
    }

    @Override
    public String getMessage()
    {
        return "No cargo with tracking id " + trackingId.idString() + " exists in the system";
    }
}