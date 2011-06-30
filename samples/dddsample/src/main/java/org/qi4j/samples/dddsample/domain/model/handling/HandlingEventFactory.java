package org.qi4j.samples.dddsample.domain.model.handling;

import org.qi4j.samples.dddsample.domain.model.cargo.TrackingId;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovementId;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;
import org.qi4j.samples.dddsample.domain.service.UnknownCarrierMovementIdException;
import org.qi4j.samples.dddsample.domain.service.UnknownLocationException;
import org.qi4j.samples.dddsample.domain.service.UnknownTrackingIdException;

import java.util.Date;

/**
 * Creates handling events.
 */
public interface HandlingEventFactory
{
    /**
     * @param completionTime    when the event was completed, for example finished loading
     * @param trackingId        tracking id
     * @param carrierMovementId carrier movement id, if applicable (may be null)
     * @param unlocode          United Nations Location Code for the location of the event
     * @param type              type of event
     *
     * @return A handling event.
     *
     * @throws UnknownCarrierMovementIdException
     *                                    if there's not carrier movement with this id
     * @throws UnknownTrackingIdException if there's no cargo with this tracking id
     * @throws UnknownLocationException   if there's no location with this UN Locode
     */
    HandlingEvent createHandlingEvent(
        Date completionTime,
        TrackingId trackingId,
        CarrierMovementId carrierMovementId,
        UnLocode unlocode,
        HandlingEvent.Type type
    )
        throws UnknownTrackingIdException, UnknownCarrierMovementIdException, UnknownLocationException;
}