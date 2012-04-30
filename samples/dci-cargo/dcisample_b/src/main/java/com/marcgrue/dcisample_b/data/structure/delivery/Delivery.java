package com.marcgrue.dcisample_b.data.structure.delivery;

import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.Date;

/**
 * Delivery
 *
 * The Delivery describes the actual transportation of the cargo, as opposed to
 * the Cargo Owner requirement {@link RouteSpecification} and the plan {@link Itinerary}.
 *
 * Complex data of the shipping domain is captured here in a value object that is
 * re-created each time some delivery status changes.
 *
 * Booking
 * The life cycle of a cargo begins with the booking procedure. During a (short) period
 * of time, between booking and initial routing, the cargo has no itinerary and is therefore
 * not routed.
 *
 * Routing
 * The Cargo Owner or booking clerk requests a list of possible routes, matching the route
 * specification, and assigns the cargo to a preferred route. The route to which a cargo is
 * assigned is described by an itinerary. The cargo is now routed.
 *
 * Cargo handling
 * Receipt of the cargo in the origin location marks the beginning of a series of handling
 * events that will eventually deliver the cargo at the destination. Handling events are
 * supposed to be reported by local handling authorities through incident logging applications
 * that then notify us of those events.
 *
 * Processing of handling events
 * When we receive handling event data from an incident logging application we validate
 * and register the event to determine what action to take. A new Delivery snapshot value
 * object is saved with the Cargo.
 *
 * Change of destination
 * It may happen that the Cargo Owner changes the destination of a cargo in the middle of a
 * voyage. The cargo route specification then no longer matches the itinerary and we say
 * that the cargo is misrouted. This will cause the system to notify the proper personel
 * and request the Cargo Owner to re-route the cargo.
 *
 * Re-routing
 * A cargo can be re-routed during transport, on demand of the Cargo Owner, in which case
 * a new itinerary is requested. The old itinerary, being a value object, is discarded and
 * a new one is attached to the cargo.
 *
 * Customs
 * The cargo can be checked by custom authorities anytime during the delivery. This doesn't
 * affect the delivery status of the cargo.
 *
 * Claim
 * The life cycle of a cargo ends when the cargo is claimed by the Cargo Owner.
 *
 * ItineraryProgressIndex
 * We have added an index that tells us how far we have come on our route. An Itinerary
 * describes a route by a list of "legs" that each describes a load time/location, a voyage
 * and an unload time/location. The itineraryProgressIndex indicates what leg is the current
 * leg. In the DDD sample the location was used to determine the current leg, but if the
 * cargo was suddenly in an unexpected location (not in the itinerary) then we wouldn't be
 * able to find out what to expect next. With the itineraryProgressIndex we can and that
 * allow us to derive a more precise delivery snapshot taking the chronological perspective
 * into account too.
 *
 * LastKnowLocation and currentVoyage didn't add much value since we can extract those from
 * the lastHandlingEvent, so we removed those.
 */
public interface Delivery
      extends ValueComposite
{
    Property<Date> timestamp();

    /* (types:)
     * RECEIVE
     * LOAD
     * UNLOAD
     * CUSTOMS
     * CLAIM
     */
    @Optional
    Property<HandlingEvent> lastHandlingEvent();

    /*
     * NOT_RECEIVED
     * IN_PORT
     * ONBOARD_CARRIER
     * CLAIMED
     * UNKNOWN
     */
    Property<TransportStatus> transportStatus();

    @UseDefaults
    Property<Boolean> isUnloadedAtDestination();

    /*
     * NOT_ROUTED
     * ROUTED
     * MISROUTED
     */
    Property<RoutingStatus> routingStatus();

    @UseDefaults
    Property<Boolean> isMisdirected();

    @Optional
    Property<Date> eta();

    // Index of earliest uncompleted Itinerary Leg - bumped up after each unload (except at destination)
    @UseDefaults
    Property<Integer> itineraryProgressIndex();

    @Optional
    Property<NextHandlingEvent> nextHandlingEvent();
}