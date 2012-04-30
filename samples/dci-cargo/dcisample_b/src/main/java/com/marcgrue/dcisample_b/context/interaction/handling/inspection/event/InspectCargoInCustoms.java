package com.marcgrue.dcisample_b.context.interaction.handling.inspection.event;

import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.InspectionException;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.InspectionFailedException;
import com.marcgrue.dcisample_b.data.structure.cargo.Cargo;
import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.delivery.Delivery;
import com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus;
import com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;
import com.marcgrue.dcisample_b.infrastructure.dci.Context;
import com.marcgrue.dcisample_b.infrastructure.dci.RoleMixin;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;

import java.util.Date;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.NOT_ROUTED;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.ONBOARD_CARRIER;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.CUSTOMS;

/**
 * Inspect Cargo In Customs (subfunction use case)
 *
 * This is one the variations of the {@link com.marcgrue.dcisample_b.context.interaction.handling.inspection.InspectCargoDeliveryStatus} use case.
 *
 * Can the cargo get handled by customs only in the current port location?! Nothing now prevents
 * an unexpected cargo in customs in some random location. A domain expert is needed to explain
 * how this goes.
 *
 * For now the location doesn't affect the misdirection status, and we presume (hope) that the
 * cargo will only get handled by customs in the port it's currently in. Should we safeguard
 * against unexpected in-customs-locations?
 */
public class InspectCargoInCustoms extends Context
{
    DeliveryInspectorRole deliveryInspector;

    HandlingEvent customsEvent;

    RouteSpecification routeSpecification;
    Itinerary itinerary;
    Integer itineraryProgressIndex;
    TransportStatus transportStatus;

    public InspectCargoInCustoms( Cargo cargo, HandlingEvent handlingEvent )
    {
        deliveryInspector = rolePlayer( DeliveryInspectorRole.class, cargo );

        customsEvent = handlingEvent;

        routeSpecification = cargo.routeSpecification().get();
        itinerary = cargo.itinerary().get();
        itineraryProgressIndex = cargo.delivery().get().itineraryProgressIndex().get();

        // Transport status before customs handling
        transportStatus = cargo.delivery().get().transportStatus().get();
    }

    public void inspect() throws InspectionException
    {
        // Pre-conditions
        if (customsEvent == null || !customsEvent.handlingEventType().get().equals( CUSTOMS ))
            throw new InspectionFailedException( "Can only inspect cargo in customs." );

        if (transportStatus.equals( ONBOARD_CARRIER ))
            throw new InspectionFailedException( "Cannot handle cargo in customs on board a carrier." );

        deliveryInspector.inspectCargoInCustoms();
    }


    @Mixins( DeliveryInspectorRole.Mixin.class )
    public interface DeliveryInspectorRole
    {
        void setContext( InspectCargoInCustoms context );

        void inspectCargoInCustoms() throws InspectionException;

        class Mixin
              extends RoleMixin<InspectCargoInCustoms>
              implements DeliveryInspectorRole
        {
            @This
            Cargo cargo;

            Delivery newDelivery;

            public void inspectCargoInCustoms() throws InspectionException
            {
                // Step 1 - Collect known delivery data

                ValueBuilder<Delivery> newDeliveryBuilder = vbf.newValueBuilder( Delivery.class );
                newDelivery = newDeliveryBuilder.prototype();
                newDelivery.timestamp().set( new Date() );
                newDelivery.lastHandlingEvent().set( c.customsEvent );
                newDelivery.transportStatus().set( IN_PORT );
                newDelivery.isUnloadedAtDestination().set( false );

                // Customs handling location doesn't affect direction
                newDelivery.isMisdirected().set( false );

                // We can't predict the next handling event from the customs event
                newDelivery.nextHandlingEvent().set( null );


                // Step 2 - Verify cargo is routed

                if (c.itinerary == null)
                {
                    newDelivery.routingStatus().set( NOT_ROUTED );
                    newDelivery.itineraryProgressIndex().set( 0 );
                    newDelivery.eta().set( null );
                }
                else if (!c.routeSpecification.isSatisfiedBy( c.itinerary ))
                {
                    newDelivery.routingStatus().set( RoutingStatus.MISROUTED );
                    newDelivery.itineraryProgressIndex().set( 0 );
                    newDelivery.eta().set( null );
                }
                else
                {
                    newDelivery.routingStatus().set( RoutingStatus.ROUTED );
                    newDelivery.itineraryProgressIndex().set( c.itineraryProgressIndex );
                    newDelivery.eta().set( c.itinerary.eta() );
                }


                // Step 3 - Save cargo delivery snapshot

                cargo.delivery().set( newDeliveryBuilder.newInstance() );
            }
        }
    }
}