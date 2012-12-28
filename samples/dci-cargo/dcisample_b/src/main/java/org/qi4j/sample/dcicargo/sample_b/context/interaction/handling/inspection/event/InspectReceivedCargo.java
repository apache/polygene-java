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
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.CargoMisdirectedException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.InspectionException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.InspectionFailedException;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.Cargo;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.Delivery;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.NextHandlingEvent;
import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Leg;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.Voyage;
import org.qi4j.sample.dcicargo.sample_b.infrastructure.dci.Context;
import org.qi4j.sample.dcicargo.sample_b.infrastructure.dci.RoleMixin;

import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.*;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.LOAD;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.RECEIVE;

/**
 * Inspect Received Cargo (subfunction use case)
 *
 * This is one the variations of the {@link com.marcgrue.dcisample_b.context.interaction.handling.inspection.InspectCargoDeliveryStatus} use case.
 *
 * Note that we consider the cargo still on track if it's received in cargo origin regardless of routing status!
 */
public class InspectReceivedCargo extends Context
{
    DeliveryInspectorRole deliveryInspector;

    HandlingEvent previousEvent;

    HandlingEvent receiveEvent;
    Location receiveLocation;
    Voyage voyage;

    RouteSpecification routeSpecification;
    Itinerary itinerary;
    Integer itineraryProgressIndex;

    public InspectReceivedCargo( Cargo cargo, HandlingEvent handlingEvent )
    {
        deliveryInspector = rolePlayer( DeliveryInspectorRole.class, cargo );
        previousEvent = cargo.delivery().get().lastHandlingEvent().get();

        receiveEvent = handlingEvent;
        receiveLocation = receiveEvent.location().get();
        voyage = receiveEvent.voyage().get();

        routeSpecification = cargo.routeSpecification().get();
        itinerary = cargo.itinerary().get();

        // Before handling
        itineraryProgressIndex = cargo.delivery().get().itineraryProgressIndex().get();
    }

    public void inspect()
        throws InspectionException
    {
        // Pre-conditions

        // Cargo has already been received before
        if( previousEvent != null && !previousEvent.equals( receiveEvent ) )
        {
            throw new InspectionFailedException( "Can't receive cargo again." );
        }

        if( receiveEvent == null || !receiveEvent.handlingEventType().get().equals( RECEIVE ) )
        {
            throw new InspectionFailedException( "Can only inspect received cargo." );
        }

        deliveryInspector.inspectReceivedCargo();
    }

    @Mixins( DeliveryInspectorRole.Mixin.class )
    public interface DeliveryInspectorRole
    {
        void setContext( InspectReceivedCargo context );

        void inspectReceivedCargo()
            throws InspectionException;

        class Mixin
            extends RoleMixin<InspectReceivedCargo>
            implements DeliveryInspectorRole
        {
            @This
            Cargo cargo;

            Delivery newDelivery;

            public void inspectReceivedCargo()
                throws InspectionException
            {
                // Step 1 - Collect known delivery data

                ValueBuilder<Delivery> newDeliveryBuilder = vbf.newValueBuilder( Delivery.class );
                newDelivery = newDeliveryBuilder.prototype();
                newDelivery.timestamp().set( new Date() );
                newDelivery.lastHandlingEvent().set( c.receiveEvent );
                newDelivery.transportStatus().set( IN_PORT );
                newDelivery.isUnloadedAtDestination().set( false );
                newDelivery.itineraryProgressIndex().set( 0 );

                // Step 2 - Verify cargo is routed

                if( c.itinerary == null )
                {
                    newDelivery.routingStatus().set( NOT_ROUTED );
                }
                else if( !c.routeSpecification.isSatisfiedBy( c.itinerary ) )
                {
                    newDelivery.routingStatus().set( MISROUTED );
                }
                else
                {
                    newDelivery.routingStatus().set( ROUTED );
                }

                if( newDelivery.routingStatus().get().equals( ROUTED ) )
                {
                    // Step 3 - Verify cargo is received in origin

                    Leg firstLeg = c.itinerary.firstLeg();
                    if( !firstLeg.loadLocation().get().equals( c.receiveEvent.location().get() ) )
                    {
                        newDelivery.isMisdirected().set( true );
                        cargo.delivery().set( newDeliveryBuilder.newInstance() );
                        throw new CargoMisdirectedException( c.receiveEvent, "Itinerary expected receipt in "
                                                                             + firstLeg.loadLocation()
                            .get()
                            .getString() );
                    }

                    newDelivery.isMisdirected().set( false );
                    newDelivery.eta().set( c.itinerary.eta() );

                    // Step 4 - Determine next expected handling event

                    ValueBuilder<NextHandlingEvent> nextHandlingEvent = vbf.newValueBuilder( NextHandlingEvent.class );
                    nextHandlingEvent.prototype().handlingEventType().set( LOAD );
                    nextHandlingEvent.prototype().location().set( firstLeg.loadLocation().get() );
                    nextHandlingEvent.prototype().time().set( firstLeg.loadTime().get() );
                    nextHandlingEvent.prototype().voyage().set( firstLeg.voyage().get() );
                    newDelivery.nextHandlingEvent().set( nextHandlingEvent.newInstance() );
                }

                // Step 5 - Save cargo delivery snapshot

                cargo.delivery().set( newDeliveryBuilder.newInstance() );
            }
        }
    }
}