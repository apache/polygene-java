package com.marcgrue.dcisample_a.data.shipping.cargo;

import com.marcgrue.dcisample_a.data.shipping.itinerary.Itinerary;
import com.marcgrue.dcisample_a.data.shipping.location.Location;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

import java.util.Date;

/**
 * A route specification describes:
 * - where a cargo is going from (origin)
 * - where a cargo is going to (destination)
 * - when a cargo is expected to arrive at the latest (deadline)
 *
 * If the route specification needs to change because of unexpected
 * handling events, it's replaced with a new one.
 *
 * A route specification "is satisfied" when an itinerary meets the requirements:
 * - First leg of itinerary has same location as origin in route specification
 * - Last leg of itinerary has same location as destination in route specification
 * - Arrival deadline is before deadline of route specification
 *
 * DCI Data is supposed to be dumb. Can we accept to have the specification
 * logic here?
 *
 * All properties are mandatory and immutable.
 * If the destination needs to change, a new RouteSpecification value object is created.
 */
@Mixins( RouteSpecification.Mixin.class )
public interface RouteSpecification
{
    Property<Location> origin();

    Property<Location> destination();

    Property<Date> arrivalDeadline();

    // Can we accept to have this "intelligent" logic here?
    // DCI Data is supposed to be dumb, but it's really convenient to have this logic here,
    // and there's no dependencies involved...
    boolean isSatisfiedBy( Itinerary itinerary );

    abstract class Mixin
          implements RouteSpecification
    {
        public boolean isSatisfiedBy( Itinerary itinerary )
        {
            return itinerary != null &&
                  !itinerary.legs().get().isEmpty() &&
                  origin().get().equals( itinerary.firstLeg().loadLocation().get() ) &&
                  destination().get().equals( itinerary.lastLeg().unloadLocation().get() ) &&
                  arrivalDeadline().get().after( itinerary.finalArrivalDate() );
        }
    }
}
