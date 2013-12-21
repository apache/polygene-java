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
package org.qi4j.sample.dcicargo.sample_b.context.test.booking.specification;

import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.sample.dcicargo.sample_b.bootstrap.test.TestApplication;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.booking.specification.DeriveUpdatedRouteSpecification;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.CargoAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.HandlingEventAggregateRoot;

import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.NOT_RECEIVED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.ONBOARD_CARRIER;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.LOAD;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.RECEIVE;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.UNLOAD;

public class DeriveUpdatedRouteSpecTest extends TestApplication
{
    private HandlingEventAggregateRoot HANDLING_EVENTS;

    @Before
    public void prepareTest()
        throws Exception
    {
        super.prepareTest();
        UnitOfWork uow = module.currentUnitOfWork();
        HANDLING_EVENTS = uow.get( HandlingEventAggregateRoot.class, HandlingEventAggregateRoot.HANDLING_EVENTS_ID );
        CargoAggregateRoot CARGOS = uow.get( CargoAggregateRoot.class, CargoAggregateRoot.CARGOS_ID );

        // Create new cargo
        routeSpec = routeSpecFactory.build( HONGKONG, STOCKHOLM, TODAY, deadline = DAY24 );
        delivery = delivery( TODAY, NOT_RECEIVED, ROUTED, unknownLeg );
        cargo = CARGOS.createCargo( routeSpec, delivery, "ABC" );
        cargo.itinerary().set( itinerary );
        trackingId = cargo.trackingId().get();
    }

    @Test
    public void deviation_1a_Destination_changed()
        throws Exception
    {
        cargo.routeSpecification().set( routeSpec );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, HONGKONG, noVoyage );
        cargo.delivery().set( delivery( handlingEvent, IN_PORT, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg1,
                                        nextHandlingEvent( LOAD, HONGKONG, DAY1, V201 ) ) );

        assertRouteSpec( HONGKONG, STOCKHOLM, TODAY, DAY24 );

        newRouteSpec = new DeriveUpdatedRouteSpecification( cargo, ROTTERDAM ).getRouteSpec();
        cargo.routeSpecification().set( newRouteSpec );

        assertRouteSpec( HONGKONG,  // Unchanged
                         ROTTERDAM, // New destination
                         DAY1,      // Completion time of last handling event
                         DAY24 );   // Unchanged
    }

    @Test
    public void step_1_Destination_unchanged()
        throws Exception
    {
        cargo.routeSpecification().set( routeSpec );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, HONGKONG, noVoyage );
        cargo.delivery().set( delivery( handlingEvent, IN_PORT, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg1,
                                        nextHandlingEvent( LOAD, HONGKONG, DAY1, V201 ) ) );

        assertRouteSpec( HONGKONG, STOCKHOLM, TODAY, DAY24 );

        newRouteSpec = new DeriveUpdatedRouteSpecification( cargo ).getRouteSpec();
        cargo.routeSpecification().set( newRouteSpec );

        assertRouteSpec( HONGKONG,  // Unchanged
                         STOCKHOLM, // Unchanged
                         DAY1,      // Completion time of last handling event
                         DAY24 );   // Unchanged
    }

    @Test
    public void deviation_2a_NotReceived()
        throws Exception
    {
        cargo.routeSpecification().set( routeSpec );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, ROUTED, unknownLeg ) );

        assertRouteSpec( HONGKONG, STOCKHOLM, TODAY, DAY24 );

        newRouteSpec = new DeriveUpdatedRouteSpecification( cargo ).getRouteSpec();
        cargo.routeSpecification().set( newRouteSpec );

        assertRouteSpec( HONGKONG,  // Unchanged
                         STOCKHOLM, // Unchanged
                         TODAY,     // Unchanged
                         DAY24 );   // Unchanged
    }

    @Test
    public void deviation_2b_OnBoardCarrier()
        throws Exception
    {
        cargo.routeSpecification().set( routeSpec );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, LOAD, HONGKONG, V201 );
        cargo.delivery().set( delivery( handlingEvent, ONBOARD_CARRIER, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg1,
                                        nextHandlingEvent( UNLOAD, CHICAGO, DAY5, V201 ) ) );

        assertRouteSpec( HONGKONG, STOCKHOLM, TODAY, DAY24 );

        newRouteSpec = new DeriveUpdatedRouteSpecification( cargo ).getRouteSpec();
        cargo.routeSpecification().set( newRouteSpec );

        assertRouteSpec( CHICAGO,   // Arrival location of current carrier movement
                         STOCKHOLM, // Unchanged
                         DAY5,      // Arrival time of current carrier movement
                         DAY24 );   // Unchanged
    }

    @Test
    public void step_3_InPort()
        throws Exception
    {
        cargo.routeSpecification().set( routeSpec );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY5, DAY5, trackingId, UNLOAD, CHICAGO, V201 );
        cargo.delivery().set( delivery( handlingEvent, IN_PORT, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg2,
                                        nextHandlingEvent( LOAD, CHICAGO, DAY5, V201 ) ) );

        assertRouteSpec( HONGKONG, STOCKHOLM, TODAY, DAY24 );

        newRouteSpec = new DeriveUpdatedRouteSpecification( cargo ).getRouteSpec();
        cargo.routeSpecification().set( newRouteSpec );

        assertRouteSpec( CHICAGO,   // Current location
                         STOCKHOLM, // Unchanged
                         DAY5,      // Last completion time
                         DAY24 );   // Unchanged
    }
}
