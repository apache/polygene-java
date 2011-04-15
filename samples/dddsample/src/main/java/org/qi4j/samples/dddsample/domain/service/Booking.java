package org.qi4j.samples.dddsample.domain.service;

import org.qi4j.api.query.Query;
import org.qi4j.samples.dddsample.domain.model.cargo.Itinerary;
import org.qi4j.samples.dddsample.domain.model.cargo.TrackingId;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;

/**
 * Cargo booking service.
 */
public interface Booking
{

    /**
     * Registers a new cargo in the tracking system, not yet routed.
     *
     * @param origin      cargo origin
     * @param destination cargo destination
     *
     * @return Cargo tracking id
     */
    TrackingId bookNewCargo( UnLocode origin, UnLocode destination );

    /**
     * Requests a list of itineraries describing possible routes for this cargo.
     *
     * @param trackingId cargo tracking id
     *
     * @return A list of possible itineraries for this cargo
     */
    Query<Itinerary> requestPossibleRoutesForCargo( TrackingId trackingId );

    /**
     * Assigns a cargo to route.
     *
     * @param trackingId cargo tracking id
     * @param itinerary  the new itinerary describing the route
     */
    void assignCargoToRoute( TrackingId trackingId, Itinerary itinerary );
}