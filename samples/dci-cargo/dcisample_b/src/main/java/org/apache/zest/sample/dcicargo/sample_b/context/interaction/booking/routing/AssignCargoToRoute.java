/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.sample.dcicargo.sample_b.context.interaction.booking.routing;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.booking.exception.RoutingException;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.booking.exception.UnsatisfyingRouteException;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.booking.specification.DeriveUpdatedRouteSpecification;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.InspectionException;
import org.apache.zest.sample.dcicargo.sample_b.data.factory.exception.CannotCreateRouteSpecificationException;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.cargo.Cargo;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.Delivery;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.NextHandlingEvent;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.voyage.CarrierMovement;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.voyage.Voyage;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.dci.Context;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.dci.RoleMixin;

import static org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.CLAIMED;
import static org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.NOT_RECEIVED;
import static org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.ONBOARD_CARRIER;
import static org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.LOAD;
import static org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.RECEIVE;
import static org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.UNLOAD;

/**
 * Assign Cargo to Route (subfunction use case)
 *
 * The booking application presents some routes that the cargo can take and the Cargo Owner chooses
 * a preferred route that we then assign to the cargo here.
 *
 * This is step 4 in the Route Cargo use case.
 */
public class AssignCargoToRoute extends Context
{
    private CargoInspectorRole cargoInspector;

    private RouteSpecification routeSpecification;
    private TransportStatus transportStatus;
    private HandlingEvent lastHandlingEvent;

    private Itinerary itinerary;

    public AssignCargoToRoute( Cargo cargo, Itinerary itinerary )
    {
        cargoInspector = rolePlayer( CargoInspectorRole.class, cargo );

        routeSpecification = cargo.routeSpecification().get();
        transportStatus = cargo.delivery().get().transportStatus().get();
        lastHandlingEvent = cargo.delivery().get().lastHandlingEvent().get();

        this.itinerary = itinerary;
    }

    public AssignCargoToRoute( String trackingIdString, Itinerary itinerary )
    {
        this( loadEntity( Cargo.class, trackingIdString ), itinerary );
    }

    public void assign()
        throws CannotCreateRouteSpecificationException, UnsatisfyingRouteException, InspectionException, RoutingException
    {
        // Pre-conditions
        if( transportStatus.equals( CLAIMED ) )
        {
            throw new RoutingException( "Can't re-route claimed cargo" );
        }

        cargoInspector.assignCargoToRoute();
    }

    @Mixins( CargoInspectorRole.Mixin.class )
    public interface CargoInspectorRole
    {
        void setContext( AssignCargoToRoute context );

        void assignCargoToRoute()
            throws CannotCreateRouteSpecificationException, UnsatisfyingRouteException, InspectionException;

        class Mixin
            extends RoleMixin<AssignCargoToRoute>
            implements CargoInspectorRole
        {
            @This
            Cargo cargo;

            RouteSpecification newRouteSpec;
            NextHandlingEvent nextHandlingEvent;
            Delivery newDelivery;

            public void assignCargoToRoute()
                throws CannotCreateRouteSpecificationException, UnsatisfyingRouteException, InspectionException
            {
                // Step 1 - Derive updated route specification

                newRouteSpec = new DeriveUpdatedRouteSpecification( cargo ).getRouteSpec();

                // Step 2 - Verify that route satisfies route specification

                if( !newRouteSpec.isSatisfiedBy( c.itinerary ) )
                {
                    throw new UnsatisfyingRouteException( newRouteSpec, c.itinerary );
                }

                // Step 3 - Assign new route specification to cargo

                cargo.routeSpecification().set( newRouteSpec );

                // Step 4 - Assign cargo to route

                cargo.itinerary().set( c.itinerary );

                // Step 5 - Determine next handling event

                ValueBuilder<NextHandlingEvent> nextHandlingEventBuilder = vbf.newValueBuilder( NextHandlingEvent.class );
                nextHandlingEvent = nextHandlingEventBuilder.prototype();

                if( c.transportStatus.equals( NOT_RECEIVED ) )
                {
                    // Routed unhandled cargo is expected to be received in origin.
                    nextHandlingEvent.handlingEventType().set( RECEIVE );
                    nextHandlingEvent.location().set( c.itinerary.firstLeg().loadLocation().get() );
                }
                else if( c.transportStatus.equals( ONBOARD_CARRIER ) )
                {
                    // Re-routed cargo onboard carrier is expected to be unloaded in next port (regardless of new itinerary).
                    Voyage voyage = c.lastHandlingEvent.voyage().get();
                    CarrierMovement carrierMovement = voyage.carrierMovementDepartingFrom( c.lastHandlingEvent
                                                                                               .location()
                                                                                               .get() );

                    // Estimate carrier arrival time
                    LocalDate estimatedArrivalDate = carrierMovement.arrivalDate().get();
                    if( c.lastHandlingEvent.completionDate().get().isAfter( carrierMovement.departureDate().get() ) )
                    {
                        LocalDate start = carrierMovement.departureDate().get();
                        LocalDate end = carrierMovement.arrivalDate().get();
                        Duration duration = Duration.between( start, end );
                        estimatedArrivalDate = c.lastHandlingEvent.completionDate().get().plus(duration);
                    }

                    nextHandlingEvent.handlingEventType().set( UNLOAD );
                    nextHandlingEvent.location().set( carrierMovement.arrivalLocation().get() );
                    nextHandlingEvent.date().set( estimatedArrivalDate );
                    nextHandlingEvent.voyage().set( c.lastHandlingEvent.voyage().get() );
                }
                else // IN_PORT
                {
                    // Re-routed cargo in port is expected to be loaded onto first carrier of new itinerary.
                    nextHandlingEvent.handlingEventType().set( LOAD );
                    nextHandlingEvent.location().set( c.itinerary.firstLeg().loadLocation().get() );
                    nextHandlingEvent.date().set( c.itinerary.firstLeg().loadDate().get() );
                    nextHandlingEvent.voyage().set( c.itinerary.firstLeg().voyage().get() );
                }

                // Step 6 - Update cargo delivery status

                ValueBuilder<Delivery> deliveryBuilder = vbf.newValueBuilder( Delivery.class );
                newDelivery = deliveryBuilder.prototype();
                newDelivery.timestamp().set( Instant.now() );
                newDelivery.lastHandlingEvent().set( c.lastHandlingEvent );
                newDelivery.transportStatus().set( c.transportStatus );
                newDelivery.isUnloadedAtDestination().set( false );
                newDelivery.routingStatus().set( ROUTED );
                newDelivery.isMisdirected().set( false );
                newDelivery.eta().set( c.itinerary.eta() );
                newDelivery.itineraryProgressIndex().set( 0 );
                newDelivery.nextHandlingEvent().set( nextHandlingEventBuilder.newInstance() );

                cargo.delivery().set( deliveryBuilder.newInstance() );
            }
        }
    }
}