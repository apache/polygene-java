package com.marcgrue.dcisample_a.context.shipping.booking;

import com.marcgrue.dcisample_a.data.shipping.cargo.Cargo;
import com.marcgrue.dcisample_a.data.shipping.cargo.RouteSpecification;
import com.marcgrue.dcisample_a.data.shipping.delivery.Delivery;
import com.marcgrue.dcisample_a.data.shipping.delivery.ExpectedHandlingEvent;
import com.marcgrue.dcisample_a.data.shipping.delivery.RoutingStatus;
import com.marcgrue.dcisample_a.data.shipping.delivery.TransportStatus;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEvent;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEventType;
import com.marcgrue.dcisample_a.data.shipping.itinerary.Itinerary;
import com.marcgrue.dcisample_a.data.shipping.itinerary.Leg;
import com.marcgrue.dcisample_a.data.shipping.location.Location;
import com.marcgrue.dcisample_a.data.shipping.voyage.Voyage;
import com.marcgrue.dcisample_a.infrastructure.dci.Context;
import com.marcgrue.dcisample_a.infrastructure.dci.RoleMixin;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;

import java.util.Date;
import java.util.Iterator;

/**
 * Build Delivery Snapshot use case.
 *
 * This is a subfunction level use case (see Cockurn 2001) supporting user-goal level use cases
 * like {@link BookNewCargo} by returning a Delivery snapshot Value object.
 *
 * A Delivery snapshot describes all we know about the current shipping status of a Cargo. It's the
 * heart of the application domain knowledge.
 *
 * The Delivery snapshot is derived from one of the 3 overall stages a Cargo shipping can be in:
 * 1) Cargo is being created  (Route Specification is known)
 * 2) Cargo has been routed   (Route Specification + Itinerary is known)
 * 3) Cargo is in transit     (Route Specification + Itinerary + last Handling Event is known)
 *
 * Code is organized along the contours of the process flow of cargo shipping instead of the data
 * structures. This makes it easier to reason about a certain step in the shipping process (use case)
 * according to the intuitive understanding of the shipping domain.
 *
 * @see RouteSpecification  plays a (methodful) Role of a factory that coordinates the Delivery build.
 * @see HandlingEvent       plays a (methodful) Role that coordinates deriving data from the last Handling Event.
 * @see Itinerary           plays a (methodful) Role supporting the other two roles with Itinerary calculations.
 */
public class BuildDeliverySnapshot extends Context
{
    // ROLES ---------------------------------------------------------------------

    private FactoryRole factory;
    private ItineraryRole itinerary;
    private HandlingEventRole handlingEvent;

    private Delivery newDeliverySnapshot;


    // CONTEXT CONSTRUCTORS ------------------------------------------------------

    public BuildDeliverySnapshot( RouteSpecification routeSpecification )
    {
        // Deviation 2a
        if (routeSpecification.origin().get() == routeSpecification.destination().get())
            throw new RuntimeException( "Route specification is invalid. Origin equals destination." );

        // Deviation 2b
        if (routeSpecification.arrivalDeadline().get().before( new Date() ))
            throw new RuntimeException( "Arrival deadline is in the past or Today." +
                                              "\nDeadline " + routeSpecification.arrivalDeadline().get() +
                                              "\nToday    " + new Date() );

        factory = rolePlayer( FactoryRole.class, routeSpecification );
        itinerary = null;
        handlingEvent = null;
    }

    public BuildDeliverySnapshot( Cargo cargo )
    {
        factory = rolePlayer( FactoryRole.class, cargo.routeSpecification().get() );
        itinerary = rolePlayer( ItineraryRole.class, cargo.itinerary().get() );
        handlingEvent = rolePlayer( HandlingEventRole.class, cargo.delivery().get().lastHandlingEvent().get() );
    }

    public BuildDeliverySnapshot( Cargo cargo, HandlingEvent registeredHandlingEvent )
    {
        factory = rolePlayer( FactoryRole.class, cargo.routeSpecification().get() );
        itinerary = rolePlayer( ItineraryRole.class, cargo.itinerary().get() );
        handlingEvent = rolePlayer( HandlingEventRole.class, registeredHandlingEvent );
    }

    // INTERACTIONS --------------------------------------------------------------

    public Delivery get()
    {
        return factory.deriveDeliverySnapshot();
    }


    // METHODFUL ROLE IMPLEMENTATIONS --------------------------------------------

    /**
     * The FactoryRole coordinates the overall Delivery snapshot build.
     *
     * When the Cargo has a delivery history the FactoryRole delegates to the LastHandlingEventRole
     * to derive data from the last HandlingEvent.
     */
    @Mixins( FactoryRole.Mixin.class )
    public interface FactoryRole
    {
        void setContext( BuildDeliverySnapshot context );

        Delivery deriveDeliverySnapshot();

        class Mixin
              extends RoleMixin<BuildDeliverySnapshot>
              implements FactoryRole
        {
            @This
            RouteSpecification routeSpecification;

            ValueBuilder<Delivery> deliveryBuilder;

            public Delivery deriveDeliverySnapshot()
            {
                /*
               * Default values:
               *
               * isMisdirected = false
               * nextExpectedHandlingEvent = null
               * lastHandlingEvent = null
               * lastKnownLocation = null
               * currentVoyage = null
               * eta = null
               * isUnloadedAtDestination = false
               *
               * */

                // Build delivery snapshot object
                deliveryBuilder = vbf.newValueBuilder( Delivery.class );
                context.newDeliverySnapshot = deliveryBuilder.prototype();
                context.newDeliverySnapshot.timestamp().set( new Date() );

                // Deviation 2c: Cargo is not routed yet
                if (context.itinerary == null)
                    return deriveWithRouteSpecification();

                if (!routeSpecification.isSatisfiedBy( (Itinerary) context.itinerary ))
                {
                    // Deviation 2d: Itinerary not satisfying
                    context.newDeliverySnapshot.routingStatus().set( RoutingStatus.MISROUTED );
                }
                else
                {
                    // Step 2
                    context.newDeliverySnapshot.routingStatus().set( RoutingStatus.ROUTED );
                    context.newDeliverySnapshot.eta().set( context.itinerary.eta() );
                }

                // Deviation 3a: Cargo has no handling history yet
                if (context.handlingEvent == null)
                    return deriveWithItinerary();

                // Step 4: Cargo has a handling history
                context.handlingEvent.deriveWithHandlingEvent();

                // Step 5: Return Delivery object
                return deliveryBuilder.newInstance();
            }

            private Delivery deriveWithRouteSpecification()
            {
                // Deviation 2c
                context.newDeliverySnapshot.routingStatus().set( RoutingStatus.NOT_ROUTED );
                context.newDeliverySnapshot.transportStatus().set( TransportStatus.NOT_RECEIVED );
                context.newDeliverySnapshot.nextExpectedHandlingEvent().set( buildExpectedReceiveEvent() );
                return deliveryBuilder.newInstance();
            }

            private Delivery deriveWithItinerary()
            {
                context.newDeliverySnapshot.transportStatus().set( TransportStatus.NOT_RECEIVED );

                // Deviation 3a.1.a
                if (context.newDeliverySnapshot.routingStatus().get() == RoutingStatus.ROUTED)
                {
                    context.newDeliverySnapshot.nextExpectedHandlingEvent().set( buildExpectedReceiveEvent() );
                }

                return deliveryBuilder.newInstance();
            }

            private ExpectedHandlingEvent buildExpectedReceiveEvent()
            {
                ValueBuilder<ExpectedHandlingEvent> builder = vbf.newValueBuilder( ExpectedHandlingEvent.class );
                builder.prototype().handlingEventType().set( HandlingEventType.RECEIVE );
                builder.prototype().location().set( routeSpecification.origin().get() );
                return builder.newInstance();
            }
        }
    }

    /**
     * The HandlingEventRole derives data from the last Handling Event.
     *
     * This is where the core domain knowledge about the shipping flow is. Code is organized by the
     * 6 different handling event types of a cargo:
     * RECEIVE - LOAD - UNLOAD - CLAIM - CUSTOMS - (UNKNOWN)
     *
     * For each HandlingEventType we can reason about the status and what is expected next. This
     * follows the real world shipping flow rather than data structures of the domain.
     *
     * The HandlingEventRole uses the ItineraryRole heavily to calculate values based on Itinerary data.
     */
    @Mixins( HandlingEventRole.Mixin.class )
    public interface HandlingEventRole
    {
        void setContext( BuildDeliverySnapshot context );

        void deriveWithHandlingEvent();

        class Mixin
              extends RoleMixin<BuildDeliverySnapshot>
              implements HandlingEventRole
        {
            /*
           * HandlingEvent can be from last Delivery snapshot (last expected HandlingEvent) or
           * a new registered HandlingEvent.
           * */
            @This
            HandlingEvent handlingEvent;

            // Local convenience fields
            Location lastKnownLocation;
            Voyage currentVoyage;
            Boolean cargoIsNotMisrouted;

            // Flag for determining if we should wasUnexpected a former misdirected Handling Event after a reroute
            Boolean cargoIsRerouted;

            public void deriveWithHandlingEvent()
            {
                lastKnownLocation = handlingEvent.location().get();
                currentVoyage = handlingEvent.voyage().get();
                cargoIsNotMisrouted = context.newDeliverySnapshot.routingStatus().get() == RoutingStatus.ROUTED;

                // Cargo has been rerouted when the last handling event is declared unexpected.
                cargoIsRerouted = handlingEvent.wasUnexpected().get();

                // Step 3
                context.newDeliverySnapshot.lastHandlingEvent().set( handlingEvent );
                context.newDeliverySnapshot.lastKnownLocation().set( lastKnownLocation );

                // Step 4
                switch (handlingEvent.handlingEventType().get())
                {
                    case RECEIVE:
                        cargoReceived();        // Deviation 4a
                        return;

                    case LOAD:
                        cargoLoaded();          // Deviation 4b
                        return;

                    case UNLOAD:
                        cargoUnloaded();        // Deviation 4c
                        return;

                    case CUSTOMS:
                        cargoInCustoms();       // Deviation 4d
                        return;

                    case CLAIM:
                        cargoClaimed();         // Deviation 4e
                        return;

                    default:
                        unknownHandlingEvent(); // Deviation 4f
                }
            }

            // Deviation 4a
            private void cargoReceived()
            {
                context.newDeliverySnapshot.transportStatus().set( TransportStatus.IN_PORT );

                if (!cargoIsRerouted && !context.itinerary.expectsOrigin( lastKnownLocation ))
                {
                    context.newDeliverySnapshot.eta().set( null );
                    context.newDeliverySnapshot.isMisdirected().set( true );
                }
                else if (cargoIsNotMisrouted)
                {
                    ExpectedHandlingEvent expectedEvent = context.itinerary.expectedEventAfterReceive();
                    context.newDeliverySnapshot.nextExpectedHandlingEvent().set( expectedEvent );
                }
            }

            // Deviation 4b
            private void cargoLoaded()
            {
                context.newDeliverySnapshot.transportStatus().set( TransportStatus.ONBOARD_CARRIER );
                context.newDeliverySnapshot.currentVoyage().set( currentVoyage );

                if (cargoIsRerouted)
                {
                    // After a reroute following a load in an unexpected location, we expected the cargo to be
                    // unloaded at the unload location of the first leg of the new itinerary.
                    ExpectedHandlingEvent expectedEvent = context.itinerary.expectedEventAfterLoadAt( lastKnownLocation );
                    context.newDeliverySnapshot.nextExpectedHandlingEvent().set( expectedEvent );
                }
                else if (!context.itinerary.expectsLoad( lastKnownLocation, currentVoyage ))
                {
                    context.newDeliverySnapshot.eta().set( null );
                    context.newDeliverySnapshot.isMisdirected().set( true );
                }
                else if (cargoIsNotMisrouted)
                {
                    ExpectedHandlingEvent expectedEvent = context.itinerary.expectedEventAfterLoadAt( lastKnownLocation );
                    context.newDeliverySnapshot.nextExpectedHandlingEvent().set( expectedEvent );
                }
            }

            // Deviation 4c
            private void cargoUnloaded()
            {
                context.newDeliverySnapshot.transportStatus().set( TransportStatus.IN_PORT );

                if (cargoIsRerouted)
                {
                    // After a reroute following an unload in an unexpected location, we expected the cargo
                    // to be loaded onto a carrier at the first location of the new route specification.
                    ExpectedHandlingEvent expectedEvent = context.itinerary.expectedEventAfterReceive();
                    context.newDeliverySnapshot.nextExpectedHandlingEvent().set( expectedEvent );
                }
                else if (!context.itinerary.expectsUnload( lastKnownLocation, currentVoyage ))
                {
                    context.newDeliverySnapshot.eta().set( null );
                    context.newDeliverySnapshot.isMisdirected().set( true );
                }
                else if (cargoIsNotMisrouted)
                {
                    ExpectedHandlingEvent expectedEvent = context.itinerary.expectedEventAfterUnloadAt( lastKnownLocation );
                    context.newDeliverySnapshot.nextExpectedHandlingEvent().set( expectedEvent );

                    Location expectedDestination = ( (RouteSpecification) context.factory ).destination().get();
                    context.newDeliverySnapshot.isUnloadedAtDestination().set( lastKnownLocation.equals( expectedDestination ) );
                }
            }

            // Deviation 4d
            private void cargoInCustoms()
            {
                context.newDeliverySnapshot.transportStatus().set( TransportStatus.IN_PORT );

                Location expectedDestination = ( (RouteSpecification) context.factory ).destination().get();
                context.newDeliverySnapshot.isUnloadedAtDestination().set( lastKnownLocation.equals( expectedDestination ) );

            }

            // Deviation 4e
            private void cargoClaimed()
            {
                context.newDeliverySnapshot.transportStatus().set( TransportStatus.CLAIMED );

                Location expectedDestination = ( (RouteSpecification) context.factory ).destination().get();
                context.newDeliverySnapshot.isUnloadedAtDestination().set( lastKnownLocation.equals( expectedDestination ) );


                if (!context.itinerary.expectsDestination( lastKnownLocation ))
                {
                    context.newDeliverySnapshot.eta().set( null );
                    context.newDeliverySnapshot.isMisdirected().set( true );
                }
            }

            // Deviation 4f
            private void unknownHandlingEvent()
            {
                context.newDeliverySnapshot.transportStatus().set( TransportStatus.UNKNOWN );
            }
        }
    }

    /**
     * The ItineraryRole supports the HandlingEventRole with calculated results derived from Itinerary Legs.
     */
    @Mixins( ItineraryRole.Mixin.class )
    public interface ItineraryRole
    {
        void setContext( BuildDeliverySnapshot context );

        Date eta();

        boolean expectsOrigin( Location location );
        boolean expectsLoad( Location location, Voyage voyage );
        boolean expectsUnload( Location location, Voyage voyage );
        boolean expectsDestination( Location location );

        ExpectedHandlingEvent expectedEventAfterReceive();
        ExpectedHandlingEvent expectedEventAfterLoadAt( Location lastLoadLocation );
        ExpectedHandlingEvent expectedEventAfterUnloadAt( Location lastUnloadLocation );

        class Mixin
              extends RoleMixin<BuildDeliverySnapshot>
              implements ItineraryRole
        {
            @This
            Itinerary itinerary;

            public Date eta()
            {
                return itinerary.lastLeg().unloadTime().get();
            }

            // Route expectations ----------------------------------------------------

            public boolean expectsOrigin( Location location )
            {
                return itinerary.firstLeg().loadLocation().get().equals( location );
            }

            public boolean expectsLoad( Location location, Voyage voyage )
            {
                // One leg with same load location and voyage
                for (Leg leg : itinerary.legs().get())
                    if (leg.loadLocation().get().equals( location ) && leg.voyage().get().equals( voyage ))
                        return true;
                return false;
            }

            public boolean expectsUnload( Location location, Voyage voyage )
            {
                // One leg with same unload location and voyage
                for (Leg leg : itinerary.legs().get())
                    if (leg.unloadLocation().get().equals( location ) && leg.voyage().get().equals( voyage ))
                        return true;
                return false;
            }

            public boolean expectsDestination( Location location )
            {
                // Last leg destination matches
                return ( itinerary.lastLeg().unloadLocation().get().equals( location ) );
            }

            // Next expected handling event ----------------------------------------------

            public ExpectedHandlingEvent expectedEventAfterReceive()
            {
                // After RECEIVE, expect LOAD location and voyage of first itinerary leg
                final Leg firstLeg = itinerary.legs().get().iterator().next();
                return buildEvent( HandlingEventType.LOAD, firstLeg.loadLocation().get(), firstLeg.loadTime().get(), firstLeg.voyage().get() );
            }

            public ExpectedHandlingEvent expectedEventAfterLoadAt( Location lastLoadLocation )
            {
                // After LOAD, expect UNLOAD location and voyage of same itinerary leg as LOAD
                for (Leg leg : itinerary.legs().get())
                    if (leg.loadLocation().get().equals( lastLoadLocation ))
                        return buildEvent( HandlingEventType.UNLOAD, leg.unloadLocation().get(), leg.unloadTime().get(), leg.voyage().get() );
                return null;
            }

            public ExpectedHandlingEvent expectedEventAfterUnloadAt( Location lastUnloadLocation )
            {
                // After UNLOAD, expect LOAD location and voyage of following itinerary leg, or CLAIM if no more legs
                for (Iterator<Leg> it = itinerary.legs().get().iterator(); it.hasNext(); )
                {
                    final Leg leg = it.next();
                    if (leg.unloadLocation().get().equals( lastUnloadLocation ))
                    {
                        // Cargo has a matching unload location in itinerary

                        if (it.hasNext())
                        {
                            // Cargo has not arrived yet (uncompleted legs in itinerary)
                            // We expect it to be loaded onto some Carrier
                            final Leg nextLeg = it.next();
                            return buildEvent( HandlingEventType.LOAD, nextLeg.loadLocation().get(), nextLeg.loadTime().get(), nextLeg.voyage().get() );
                        }
                        else
                        {
                            // Cargo has arrived (no more legs in itinerary)
                            // We expect it to be claimed by the customer
                            return buildEvent( HandlingEventType.CLAIM, leg.unloadLocation().get(), leg.unloadTime().get(), null );
                        }
                    }
                }

                // Itinerary doesn't recognize last unload location
                return null;
            }

            private ExpectedHandlingEvent buildEvent( HandlingEventType eventType, Location location, Date time, Voyage voyage )
            {
                ValueBuilder<ExpectedHandlingEvent> builder = vbf.newValueBuilder( ExpectedHandlingEvent.class );
                builder.prototype().handlingEventType().set( eventType );
                builder.prototype().location().set( location );
                builder.prototype().time().set( time );
                builder.prototype().voyage().set( voyage );
                return builder.newInstance();
            }
        }
    }
}