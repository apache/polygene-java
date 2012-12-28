/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.event;

import java.util.Date;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.CargoArrivedException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.InspectionException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.InspectionFailedException;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.Cargo;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.Delivery;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.NextHandlingEvent;
import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;
import org.qi4j.sample.dcicargo.sample_b.infrastructure.dci.Context;
import org.qi4j.sample.dcicargo.sample_b.infrastructure.dci.RoleMixin;

import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.*;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.CLAIM;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.UNLOAD;

/**
 * Inspect Arrived Cargo (subfunction use case)
 *
 * This is one the variations of the {@link com.marcgrue.dcisample_b.context.interaction.handling.inspection.InspectCargoDeliveryStatus} use case.
 */
public class InspectArrivedCargo extends Context
{
    DeliveryInspectorRole deliveryInspector;

    HandlingEvent arrivalEvent;
    Location arrivalLocation;

    RouteSpecification routeSpecification;
    Location destination;

    Itinerary itinerary;
    Integer itineraryProgressIndex;

    public InspectArrivedCargo( Cargo cargo, HandlingEvent handlingEvent )
    {
        deliveryInspector = rolePlayer( DeliveryInspectorRole.class, cargo );

        arrivalEvent = handlingEvent;
        arrivalLocation = arrivalEvent.location().get();

        routeSpecification = cargo.routeSpecification().get();
        destination = routeSpecification.destination().get();
        itinerary = cargo.itinerary().get();

        // Before handling
        itineraryProgressIndex = cargo.delivery().get().itineraryProgressIndex().get();
    }

    public void inspect()
        throws InspectionException
    {
        // Pre-conditions
        if( arrivalEvent == null ||
            !arrivalEvent.handlingEventType().get().equals( UNLOAD ) ||
            !arrivalLocation.equals( destination ) )
        {
            throw new InspectionFailedException( "Can only inspect arrived cargo." );
        }

        deliveryInspector.inspectArrivedCargo();
    }

    @Mixins( DeliveryInspectorRole.Mixin.class )
    public interface DeliveryInspectorRole
    {
        void setContext( InspectArrivedCargo context );

        void inspectArrivedCargo()
            throws InspectionException;

        class Mixin
            extends RoleMixin<InspectArrivedCargo>
            implements DeliveryInspectorRole
        {
            @This
            Cargo cargo;

            Delivery newDelivery;

            public void inspectArrivedCargo()
                throws InspectionException
            {
                // Step 1 - Collect known delivery data

                ValueBuilder<Delivery> newDeliveryBuilder = vbf.newValueBuilder( Delivery.class );
                newDelivery = newDeliveryBuilder.prototype();
                newDelivery.timestamp().set( new Date() );
                newDelivery.lastHandlingEvent().set( c.arrivalEvent );
                newDelivery.transportStatus().set( IN_PORT );

                // Always on track if arrived at final destination
                newDelivery.isUnloadedAtDestination().set( true );
                newDelivery.isMisdirected().set( false );

                // Step 2 - Determine that cargo is routed (for internal reference)

                if( c.itinerary == null )
                {
                    newDelivery.routingStatus().set( NOT_ROUTED );
                    newDelivery.eta().set( null );
                    newDelivery.itineraryProgressIndex().set( 0 );
                }
                else if( !c.routeSpecification.isSatisfiedBy( c.itinerary ) )
                {
                    newDelivery.routingStatus().set( MISROUTED );
                    newDelivery.eta().set( null );
                    newDelivery.itineraryProgressIndex().set( 0 );
                }
                else
                {
                    newDelivery.routingStatus().set( ROUTED );
                    newDelivery.eta().set( c.itinerary.eta() );
                    newDelivery.itineraryProgressIndex().set( c.itineraryProgressIndex );
                }

                // Step 3 - Set next expected handling event to claim

                ValueBuilder<NextHandlingEvent> nextHandlingEvent = vbf.newValueBuilder( NextHandlingEvent.class );
                nextHandlingEvent.prototype().handlingEventType().set( CLAIM );
                nextHandlingEvent.prototype().location().set( c.arrivalLocation );
                nextHandlingEvent.prototype().time().set( c.arrivalEvent.completionTime().get() );
                nextHandlingEvent.prototype().voyage().set( null );
                newDelivery.nextHandlingEvent().set( nextHandlingEvent.newInstance() );

                // Step 4 - Save cargo delivery snapshot

                cargo.delivery().set( newDeliveryBuilder.newInstance() );

                // Step 5 - Notify cargo owner of arrival at final destination

                // (Mockup for notification to cargo owner)
                throw new CargoArrivedException( c.arrivalEvent );
            }
        }
    }
}