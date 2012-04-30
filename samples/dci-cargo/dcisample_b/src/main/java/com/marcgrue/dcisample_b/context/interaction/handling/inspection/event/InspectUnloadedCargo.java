package com.marcgrue.dcisample_b.context.interaction.handling.inspection.event;

import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.*;
import com.marcgrue.dcisample_b.data.structure.cargo.Cargo;
import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.delivery.Delivery;
import com.marcgrue.dcisample_b.data.structure.delivery.NextHandlingEvent;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;
import com.marcgrue.dcisample_b.data.structure.itinerary.Leg;
import com.marcgrue.dcisample_b.data.structure.location.Location;
import com.marcgrue.dcisample_b.data.structure.voyage.Voyage;
import com.marcgrue.dcisample_b.infrastructure.dci.Context;
import com.marcgrue.dcisample_b.infrastructure.dci.RoleMixin;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;

import java.util.Date;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.*;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.LOAD;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.UNLOAD;

/**
 * Inspect Unloaded Cargo (subfunction use case)
 *
 * This is one the variations of the {@link com.marcgrue.dcisample_b.context.interaction.handling.inspection.InspectCargoDeliveryStatus} use case.
 */
public class InspectUnloadedCargo extends Context
{
    DeliveryInspectorRole deliveryInspector;

    HandlingEvent unloadEvent;
    Location unloadLocation;
    Voyage voyage;

    RouteSpecification routeSpecification;
    Location destination;
    Itinerary itinerary;
    Integer itineraryProgressIndex;
    boolean wasMisdirected;

    public InspectUnloadedCargo( Cargo cargo, HandlingEvent handlingEvent )
    {
        deliveryInspector = rolePlayer( DeliveryInspectorRole.class, cargo );

        unloadEvent = handlingEvent;
        unloadLocation = unloadEvent.location().get();
        voyage = unloadEvent.voyage().get();

        routeSpecification = cargo.routeSpecification().get();
        destination = routeSpecification.destination().get();
        itinerary = cargo.itinerary().get();
        wasMisdirected = cargo.delivery().get().isMisdirected().get();

        // Before inspection
        itineraryProgressIndex = cargo.delivery().get().itineraryProgressIndex().get();
    }

    public void inspect() throws InspectionException
    {
        // Pre-conditions
        if (unloadEvent == null || !unloadEvent.handlingEventType().get().equals( UNLOAD ) || unloadLocation.equals( destination ))
            throw new InspectionFailedException( "Can only inspect unloaded cargo that hasn't arrived at destination." );

        deliveryInspector.inspectUnloadedCargo();
    }


    @Mixins( DeliveryInspectorRole.Mixin.class )
    public interface DeliveryInspectorRole
    {
        void setContext( InspectUnloadedCargo context );

        void inspectUnloadedCargo() throws InspectionException;

        class Mixin
              extends RoleMixin<InspectUnloadedCargo>
              implements DeliveryInspectorRole
        {
            @This
            Cargo cargo;

            Delivery newDelivery;

            public void inspectUnloadedCargo() throws InspectionException
            {
                // Step 1 - Collect known delivery data

                ValueBuilder<Delivery> newDeliveryBuilder = vbf.newValueBuilder( Delivery.class );
                newDelivery = newDeliveryBuilder.prototype();
                newDelivery.timestamp().set( new Date() );
                newDelivery.lastHandlingEvent().set( c.unloadEvent );
                newDelivery.transportStatus().set( IN_PORT );
                newDelivery.isUnloadedAtDestination().set( false );


                // Step 2 - Verify cargo is routed

                if (c.itinerary == null)
                {
                    newDelivery.routingStatus().set( NOT_ROUTED );
                    newDelivery.itineraryProgressIndex().set( 0 );
                    cargo.delivery().set( newDeliveryBuilder.newInstance() );
                    throw new CargoNotRoutedException( c.unloadEvent );
                }
                if (!c.routeSpecification.isSatisfiedBy( c.itinerary ))
                {
                    newDelivery.routingStatus().set( MISROUTED );
                    newDelivery.itineraryProgressIndex().set( 0 );
                    cargo.delivery().set( newDeliveryBuilder.newInstance() );
                    throw new CargoMisroutedException( c.unloadEvent, c.routeSpecification, c.itinerary );
                }
                newDelivery.routingStatus().set( ROUTED );
                newDelivery.eta().set( c.itinerary.eta() );

                // Current itinerary progress
                newDelivery.itineraryProgressIndex().set( c.itineraryProgressIndex );


                // Step 3 - Verify cargo is on track

                Leg plannedCarrierMovement = c.itinerary.leg( c.itineraryProgressIndex );
                if (plannedCarrierMovement == null)
                    throw new InspectionFailedException( "Itinerary progress index '" + c.itineraryProgressIndex + "' is invalid!" );


                Integer itineraryProgressIndex;
//                if (c.wasMisdirected && c.unloadLocation.equals( c.routeSpecification.origin().get() ))
                if (c.unloadLocation.equals( c.routeSpecification.origin().get() ))
//                if (c.itineraryProgressIndex == -1)
                {
                    /**
                     * Unloading in the origin of a route specification of a misdirected cargo
                     * tells us that the cargo has been re-routed (re-routing while on board a
                     * carrier sets new origin of route specification to arrival location of
                     * current carrier movement).
                     *
                     * Since the current unload was related to the old itinerary, we don't verify
                     * the misdirection status against the new itinerary.
                     *
                     * The itinerary index starts over from the first leg of the new itinerary
                     * */
                    itineraryProgressIndex = 0;
                }
                else if (!plannedCarrierMovement.unloadLocation().get().equals( c.unloadLocation ))
                {
                    newDelivery.isMisdirected().set( true );
                    cargo.delivery().set( newDeliveryBuilder.newInstance() );
                    throw new CargoMisdirectedException( c.unloadEvent, "Itinerary expected unload in "
                          + plannedCarrierMovement.unloadLocation().get() );
                }
                else if (!plannedCarrierMovement.voyage().get().equals( c.voyage ))
                {
                    // Do we care if cargo unloads from an unexpected carrier?
                    itineraryProgressIndex = c.itineraryProgressIndex + 1;
                }
                else
                {
                    // Cargo delivery has progressed and we expect a load in next itinerary leg load location
                    itineraryProgressIndex = c.itineraryProgressIndex + 1;
                }

                newDelivery.isMisdirected().set( false );

                // Modify itinerary progress index according to misdirection status
                newDelivery.itineraryProgressIndex().set( itineraryProgressIndex );


                // Step 4 - Determine next expected handling event

                Leg nextCarrierMovement = c.itinerary.leg( itineraryProgressIndex );

                ValueBuilder<NextHandlingEvent> nextHandlingEvent = vbf.newValueBuilder( NextHandlingEvent.class );
                nextHandlingEvent.prototype().handlingEventType().set( LOAD );
                nextHandlingEvent.prototype().location().set( nextCarrierMovement.loadLocation().get() );
                nextHandlingEvent.prototype().time().set( nextCarrierMovement.loadTime().get() );
                nextHandlingEvent.prototype().voyage().set( nextCarrierMovement.voyage().get() );
                newDelivery.nextHandlingEvent().set( nextHandlingEvent.newInstance() );


                // Step 5 - Save cargo delivery snapshot

                cargo.delivery().set( newDeliveryBuilder.newInstance() );
            }
        }
    }
}