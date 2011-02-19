package org.qi4j.samples.cargo.app1.model.cargo;

import org.qi4j.samples.cargo.app1.model.location.Location;
import org.qi4j.samples.cargo.app1.system.repositories.LocationRepository;
import java.util.Date;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;


/**
 *
 */
@Mixins(RouteSpecification.RouteSpecificationMixin.class)
public interface RouteSpecification extends ValueComposite {

    Location origin();

    Location destination();

    Date arrivalDeadline();

    boolean isSatisfiedBy(Itinerary itinerary);

    interface State {
        Property<String> originUnLocodeIdentity();

        Property<String> destinationUnLocodeIdentity();

        Property<Date> arrivalDeadline();
    }

    public static abstract class RouteSpecificationMixin
            implements RouteSpecification {
        @This
        private State state;

        @Service
        private LocationRepository locationRepository;

        public Location origin() {
            return locationRepository.findLocationByUnLocode(state.originUnLocodeIdentity().get());
        }

        public Location destination() {
            return locationRepository.findLocationByUnLocode(state.destinationUnLocodeIdentity().get());
        }

        public Date arrivalDeadline() {
            return state.arrivalDeadline().get();
        }

        public boolean isSatisfiedBy(final Itinerary itinerary) {
            return itinerary != null &&
                    origin().equals(itinerary.initialDepartureLocation()) &&
                    destination().equals(itinerary.finalArrivalLocation()) &&
                    arrivalDeadline().after(itinerary.finalArrivalDate());
        }
    }

}