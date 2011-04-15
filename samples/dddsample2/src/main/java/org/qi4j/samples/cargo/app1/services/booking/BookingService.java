package org.qi4j.samples.cargo.app1.services.booking;

import java.util.Date;
import java.util.List;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.samples.cargo.app1.model.cargo.Itinerary;
import org.qi4j.samples.cargo.app1.model.cargo.TrackingId;
import org.qi4j.samples.cargo.app1.system.FutureDate;
import org.qi4j.samples.cargo.app1.system.UnLocode;

/**
 * Cargo booking service.
 */
@Mixins( BookingServiceImpl.class )
public interface BookingService
{
    /**
     * Registers a new cargo in the tracking system, not yet routed.
     *
     * @param origin          cargo origin
     * @param destination     cargo destination
     * @param arrivalDeadline arrival deadline
     * @return Cargo tracking id
     */
    TrackingId bookNewCargo( @UnLocode String origin, @UnLocode String destination, @FutureDate Date arrivalDeadline );

    /**
     * Requests a list of itineraries describing possible routes for this cargo.
     *
     * @param trackingId cargo tracking id
     * @return A list of possible itineraries for this cargo
     */
    List<Itinerary> requestPossibleRoutesForCargo( TrackingId trackingId );

    /**
     * @param itinerary  itinerary describing the selected route
     * @param trackingId cargo tracking id
     */
    void assignCargoToRoute( Itinerary itinerary, TrackingId trackingId );

    /**
     * Changes the destination of a cargo.
     *
     * @param trackingId cargo tracking id
     * @param unLocode   UN locode of new destination
     */
    void changeDestination( TrackingId trackingId, @UnLocode String unLocode );
}
