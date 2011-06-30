package org.qi4j.samples.cargo.app1.model.cargo;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.samples.cargo.app1.model.handling.HandlingEvent;
import org.qi4j.samples.cargo.app1.model.location.Location;

import java.util.Date;
import java.util.List;


/**
 *
 */
@Mixins(Itinerary.ItineraryMixin.class)
public interface Itinerary extends ValueComposite {

    List<Leg> legs();

    Leg lastLeg();

    Location initialDepartureLocation();

    Date finalArrivalDate();

    Location finalArrivalLocation();

    boolean isExpected(HandlingEvent event);

    interface State {
        Property<List<Leg>> legs();
    }

    public abstract class ItineraryMixin
            implements Itinerary {

        @This
        private State state;

        public List<Leg> legs() {
            return state.legs().get();
        }

        public Leg lastLeg() {
            List<Leg> legs = state.legs().get();
            return legs.get(legs.size()-1);
        }

        public boolean isExpected(final HandlingEvent event) {
            if (legs().isEmpty()) {
                return true;
            }

            if (event.eventType() == HandlingEvent.Type.RECEIVE) {
                //Check that the first leg's origin is the event's location
                final Leg leg = legs().get(0);
                return (leg.loadLocation().equals(event.location()));
            }

            if (event.eventType() == HandlingEvent.Type.LOAD) {
                //Check that the there is one leg with same load location and voyage
                for (Leg leg : legs()) {
                    if (leg.loadLocation().equals(event.location()) &&
                            leg.voyage().equals(event.voyage())) {
                        return true;
                    }
                }
                return false;
            }

            if (event.eventType() == HandlingEvent.Type.UNLOAD) {
                //Check that the there is one leg with same unload location and voyage
                for (Leg leg : legs()) {
                    if (leg.unloadLocation().equals(event.location()) &&
                            leg.voyage().equals(event.voyage())) {
                        return true;
                    }
                }
                return false;
            }

            if (event.eventType() == HandlingEvent.Type.CLAIM) {
                //Check that the last leg's destination is from the event's location
                final Leg leg = lastLeg();
                return (leg.unloadLocation().equals(event.location()));
            }

            //HandlingEvent.Type.CUSTOMS;
            return true;
        }

        public Location initialDepartureLocation() {
            return legs().get(0).loadLocation();
        }

        public Location finalArrivalLocation() {
            return lastLeg().unloadLocation();
        }

        public Date finalArrivalDate() {
            return lastLeg().unloadTime();
        }
    }
}