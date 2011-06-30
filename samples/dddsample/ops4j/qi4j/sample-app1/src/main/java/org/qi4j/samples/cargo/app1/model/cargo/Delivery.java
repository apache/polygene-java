package org.qi4j.samples.cargo.app1.model.cargo;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.samples.cargo.app1.model.handling.HandlingEvent;
import org.qi4j.samples.cargo.app1.model.location.Location;
import org.qi4j.samples.cargo.app1.model.voyage.Voyage;
import org.qi4j.samples.cargo.app1.system.factories.HandlingActivityFactory;
import org.qi4j.samples.cargo.app1.system.repositories.HandlingEventRepository;

import java.util.Date;
import java.util.Iterator;

/**
 *
 */
@Mixins(Delivery.DeliveryMixin.class)
public interface Delivery extends ValueComposite {

    TransportStatus transportStatus();

    RoutingStatus routingStatus();

    Boolean isMisdirected();

    Location lastKnownLocation();

    Voyage currentVoyage();

    Date eta();

    HandlingActivity nextExpectedActivity();

    Boolean isUnloadedAtDestination();

    Delivery updateOnRouting(RouteSpecification routeSpecification, Itinerary itinerary);

    boolean isMisRouted();

    public interface State {
        Property<Itinerary> itinerary();

        Property<String> lastHandlingEventIdentity();

        Property<RouteSpecification> routeSpecification();
    }


    public abstract class DeliveryMixin
            implements Delivery {

        @This
        State state;

        @Service
        private HandlingActivityFactory handlingActivityFactory;

        @Service
        private HandlingEventRepository handlingEventRepository;

        private static final HandlingActivity NO_ACTIVITY = null;
        private static final Date ETA_UNKOWN = null;

        public Delivery updateOnRouting(RouteSpecification routeSpecification, Itinerary itinerary) {
            return null;
        }

        public TransportStatus transportStatus() {
            return calculateTransportStatus();
        }

        public RoutingStatus routingStatus() {
            return calculateRoutingStatus(state.itinerary().get(), state.routeSpecification().get());
        }

        public boolean isMisRouted() {
            return routingStatus().equals(RoutingStatus.MISROUTED);
        }

        public Boolean isMisdirected() {
            return calculateMisdirectionStatus(state.itinerary().get());
        }

        public Location lastKnownLocation() {
            return calculateLastKnownLocation();
        }

        public Voyage currentVoyage() {
            return calculateCurrentVoyage();
        }

        public Date eta() {
            return calculateEta(state.itinerary().get());
        }

        public HandlingActivity nextExpectedActivity() {
            return calculateNextExpectedActivity(state.routeSpecification().get(), state.itinerary().get());
        }

        public Boolean isUnloadedAtDestination() {
            return calculateUnloadedAtDestination(state.routeSpecification().get());
        }

        private boolean calculateMisdirectionStatus(Itinerary itinerary) {
            final HandlingEvent lastEvent = lastEvent();
            return lastEvent != null && !itinerary.isExpected(lastEvent);
        }

        private Voyage calculateCurrentVoyage() {
            if (transportStatus().equals(TransportStatus.ONBOARD_CARRIER) && lastEvent() != null) {
                return lastEvent().voyage();
            } else {
                return null;
            }
        }

        private HandlingActivity calculateNextExpectedActivity(RouteSpecification routeSpecification, Itinerary itinerary) {
            if (!onTrack()) {
                return NO_ACTIVITY;
            }

            if (lastEvent() == null) {
                return handlingActivityFactory.create(HandlingEvent.Type.RECEIVE, routeSpecification.origin());
            }

            switch (lastEvent().eventType()) {

                case LOAD:
                    for (Leg leg : state.itinerary().get().legs()) {
                        if (leg.loadLocation().equals(lastEvent().location())) {
                            return handlingActivityFactory.create(HandlingEvent.Type.UNLOAD, leg.unloadLocation(), leg.voyage());
                        }
                    }
                    return NO_ACTIVITY;

                case UNLOAD:
                    for (Iterator<Leg> it = itinerary.legs().iterator(); it.hasNext();) {
                        final Leg leg = it.next();
                        if (leg.unloadLocation().equals(lastEvent().location())) {
                            if (it.hasNext()) {
                                final Leg nextLeg = it.next();
                                return handlingActivityFactory.create(HandlingEvent.Type.LOAD, nextLeg.loadLocation(), nextLeg.voyage());
                            } else {
                                return handlingActivityFactory.create(HandlingEvent.Type.CLAIM, leg.unloadLocation());
                            }
                        }
                    }

                    return NO_ACTIVITY;

                case RECEIVE:
                    final Leg firstLeg = itinerary.legs().iterator().next();
                    return handlingActivityFactory.create(HandlingEvent.Type.LOAD, firstLeg.loadLocation(), firstLeg.voyage());

                case CLAIM:
                default:
                    return NO_ACTIVITY;
            }
        }

        private RoutingStatus calculateRoutingStatus(Itinerary itinerary, RouteSpecification routeSpecification) {
            if (itinerary == null) {
                return RoutingStatus.NOT_ROUTED;
            } else {
                if (routeSpecification.isSatisfiedBy(itinerary)) {
                    return RoutingStatus.ROUTED;
                } else {
                    return RoutingStatus.MISROUTED;
                }
            }
        }

        private boolean calculateUnloadedAtDestination(RouteSpecification routeSpecification) {
            final HandlingEvent lastEvent = lastEvent();
            return lastEvent != null &&
                    HandlingEvent.Type.UNLOAD.equals(lastEvent.eventType()) &&
                    routeSpecification.destination().equals(lastEvent.location());
        }

        private boolean onTrack() {
            return routingStatus().equals(RoutingStatus.ROUTED) && !isMisdirected();
        }

        private Date calculateEta(Itinerary itinerary) {
            if (onTrack()) {
                return itinerary.finalArrivalDate();
            } else {
                return ETA_UNKOWN;
            }
        }

        private TransportStatus calculateTransportStatus() {
            final HandlingEvent lastEvent = lastEvent();
            if (lastEvent == null) {
                return TransportStatus.NOT_RECEIVED;
            }

            switch (lastEvent.eventType()) {
                case LOAD:
                    return TransportStatus.ONBOARD_CARRIER;
                case UNLOAD:
                case RECEIVE:
                case CUSTOMS:
                    return TransportStatus.IN_PORT;
                case CLAIM:
                    return TransportStatus.CLAIMED;
                default:
                    return TransportStatus.UNKNOWN;
            }
        }

        private Location calculateLastKnownLocation() {
            final HandlingEvent lastEvent = lastEvent();
            if (lastEvent != null) {
                return lastEvent.location();
            } else {
                return null;
            }
        }

        private HandlingEvent lastEvent() {
            return handlingEventRepository.findHandlingEventByIdentity(state.lastHandlingEventIdentity().get());
        }
    }
}