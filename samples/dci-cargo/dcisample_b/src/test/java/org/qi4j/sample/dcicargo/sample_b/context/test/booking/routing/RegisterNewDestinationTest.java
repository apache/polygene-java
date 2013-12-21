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
package org.qi4j.sample.dcicargo.sample_b.context.test.booking.routing;

import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.sample.dcicargo.sample_b.bootstrap.test.TestApplication;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.booking.exception.ChangeDestinationException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.booking.routing.RegisterNewDestination;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.CargoMisroutedException;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.CargoAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.HandlingEventAggregateRoot;

import static org.junit.Assert.fail;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.MISROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.NOT_ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.CLAIMED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.NOT_RECEIVED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.ONBOARD_CARRIER;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.CUSTOMS;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.LOAD;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.RECEIVE;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.UNLOAD;

/**
 * {@link RegisterNewDestination} tests
 *
 * FIXME: Every test method call the one above to allow ordered execution, ie. tests are not indepedants !
 */
public class RegisterNewDestinationTest extends TestApplication
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
        delivery = delivery( TODAY, NOT_RECEIVED, NOT_ROUTED, leg1 );
        cargo = CARGOS.createCargo( routeSpec, delivery, "ABC" );
        trackingId = cargo.trackingId().get();
        delivery = cargo.delivery().get();
    }

    @Test
    public void precondition_x1_CannotChangeDestinationOfClaimedCargo()
        throws Exception
    {
        cargo.delivery().set( delivery( DAY1, CLAIMED, ROUTED, leg1 ) );
        thrown.expect( ChangeDestinationException.class, "Can't change destination of claimed cargo" );
        new RegisterNewDestination( cargo ).to( "USCHI" );
    }

    @Test
    public void deviation_1a_UnrecognizedLocation()
        throws Exception
    {
        precondition_x1_CannotChangeDestinationOfClaimedCargo();

        cargo.delivery().set( delivery( DAY1, IN_PORT, ROUTED, leg1 ) );
        thrown.expect( ChangeDestinationException.class, "Didn't recognize location 'XXXXX'" );
        new RegisterNewDestination( cargo ).to( "XXXXX" );
    }

    @Test
    public void deviation_1b_NewDestinationSameAsOldDestination()
        throws Exception
    {
        deviation_1a_UnrecognizedLocation();

        cargo.delivery().set( delivery( DAY1, IN_PORT, ROUTED, leg1 ) );
        thrown.expect( ChangeDestinationException.class, "New destination is same as old destination." );
        new RegisterNewDestination( cargo ).to( "SESTO" );
    }

    @Test
    public void step_2_NotRouted()
        throws Exception
    {
        deviation_1b_NewDestinationSameAsOldDestination();

        cargo.routeSpecification().set( routeSpec );
        cargo.delivery().set( delivery( DAY1, NOT_RECEIVED, NOT_ROUTED, leg1 ) );

        assertRouteSpec( HONGKONG, STOCKHOLM, TODAY, DAY24 );
        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        NOT_ROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );

        new RegisterNewDestination( cargo ).to( "CNSHA" );

        assertRouteSpec( HONGKONG, SHANGHAI, TODAY, DAY24 );
        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        NOT_ROUTED, directed, unknownETA, unknownLeg,
                        RECEIVE, HONGKONG, noSpecificDate, noVoyage );
    }

    @Test
    public void step_2_NotReceived()
        throws Exception
    {
        step_2_NotRouted();

        cargo.routeSpecification().set( routeSpec );
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( null, NOT_RECEIVED, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg1,
                                        nextHandlingEvent( RECEIVE, HONGKONG, noSpecificDate, noVoyage ) ) );

        assertRouteSpec( HONGKONG, STOCKHOLM, TODAY, DAY24 );

        // No last handling event
        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        ROUTED, directed, itinerary.eta(), leg1,
                        RECEIVE, HONGKONG, noSpecificDate, noVoyage );

        new RegisterNewDestination( cargo ).to( "CNSHA" );

        // Destination changed, deadline is the same
        assertRouteSpec( HONGKONG, SHANGHAI, TODAY, DAY24 );

        /**
         * Delivery status was updated in {@link InspectUnhandledCargo}
         * Still expects receipt in cargo origin (Hong Kong).
         * */
        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        RECEIVE, HONGKONG, noSpecificDate, noVoyage );
    }

    @Test
    public void step_2_Received()
        throws Exception
    {
        step_2_NotReceived();

        cargo.routeSpecification().set( routeSpec );
        cargo.itinerary().set( itinerary );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, HONGKONG, noVoyage );
        cargo.delivery().set( delivery( handlingEvent, IN_PORT, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg1,
                                        nextHandlingEvent( LOAD, HONGKONG, DAY1, V201 ) ) );

        assertRouteSpec( HONGKONG, STOCKHOLM, TODAY, DAY24 );
        assertDelivery( RECEIVE, HONGKONG, DAY1, noVoyage,
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary.eta(), leg1,
                        LOAD, HONGKONG, DAY1, V201 );

        new RegisterNewDestination( cargo ).to( "CNSHA" );

        assertRouteSpec( HONGKONG,  // Unchanged
                         SHANGHAI,  // New destination
                         DAY1,      // Completion time of last handling event
                         DAY24 );   // Unchanged

        /**
         * Delivery status was updated in {@link InspectReceivedCargo}
         * Before cargo has been re-routed we don't know which voyage the cargo is going with next.
         * */
        assertDelivery( RECEIVE, HONGKONG, DAY1, noVoyage,
                        IN_PORT, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }

    @Test
    public void deviation_2a_OnBoardCarrier()
        throws Exception
    {
        step_2_Received();

        cargo.routeSpecification().set( routeSpec );
        cargo.itinerary().set( itinerary );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, LOAD, HONGKONG, V201 );
        cargo.delivery().set( delivery( handlingEvent, ONBOARD_CARRIER, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg1,
                                        nextHandlingEvent( UNLOAD, CHICAGO, DAY5, V201 ) ) );

        assertRouteSpec( HONGKONG, STOCKHOLM, TODAY, DAY24 );
        assertDelivery( LOAD, HONGKONG, DAY1, V201,
                        ONBOARD_CARRIER, notArrived,
                        ROUTED, directed, itinerary.eta(), leg1,
                        UNLOAD, CHICAGO, DAY5, V201 );
        try
        {
            new RegisterNewDestination( cargo ).to( "CNSHA" );
            fail();
        }
        catch( CargoMisroutedException e )
        {
            assertMessage( e, "MISROUTED! Route specification is not satisfied with itinerary" );
            assertRouteSpec( CHICAGO,   // Arrival location of current carrier movement
                             SHANGHAI,  // New destination
                             DAY5,      // Arrival time of current carrier movement
                             DAY24 );   // Unchanged

            /**
             * Delivery status was updated in {@link InspectLoadedCargo}
             * We still expect unload in Chicago
             * */
            assertDelivery( LOAD, HONGKONG, DAY1, V201,
                            ONBOARD_CARRIER, notArrived,
                            MISROUTED, directed, unknownETA, unknownLeg,
                            UNLOAD, CHICAGO, DAY5, V201 );
        }
    }

    @Test
    public void deviation_2b_InPort_Unloaded()
        throws Exception
    {
        deviation_2a_OnBoardCarrier();

        cargo.routeSpecification().set( routeSpec );
        cargo.itinerary().set( itinerary );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY5, DAY5, trackingId, UNLOAD, CHICAGO, V201 );
        cargo.delivery().set( delivery( handlingEvent, IN_PORT, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg2,
                                        nextHandlingEvent( LOAD, CHICAGO, DAY5, V201 ) ) );

        assertRouteSpec( HONGKONG, STOCKHOLM, TODAY, DAY24 );
        assertDelivery( UNLOAD, CHICAGO, DAY5, V201,
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary.eta(), leg2,
                        LOAD, CHICAGO, DAY5, V201 );
        try
        {
            new RegisterNewDestination( cargo ).to( "CNSHA" );
            fail();
        }
        catch( CargoMisroutedException e )
        {
            assertMessage( e, "MISROUTED! Route specification is not satisfied with itinerary" );
            assertRouteSpec( CHICAGO,   // Current location
                             SHANGHAI,  // New destination
                             DAY5,      // Last completion time
                             DAY24 );   // Unchanged

            /**
             *  Delivery status was updated in {@link InspectUnloadedCargo}
             *  We still expect unload in Chicago
             * */
            assertDelivery( UNLOAD, CHICAGO, DAY5, V201,
                            IN_PORT, notArrived,
                            MISROUTED, directed, unknownETA, unknownLeg,
                            unknownNextHandlingEvent );
        }
    }

    @Test
    public void deviation_2b_InPort_InCustoms()
        throws Exception
    {
        deviation_2b_InPort_Unloaded();

        cargo.routeSpecification().set( routeSpec );
        cargo.itinerary().set( itinerary );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY5, DAY5, trackingId, CUSTOMS, CHICAGO, noVoyage );
        cargo.delivery().set( delivery( handlingEvent, IN_PORT, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg2,
                                        unknownNextHandlingEvent ) );

        assertRouteSpec( HONGKONG, STOCKHOLM, TODAY, DAY24 );
        assertDelivery( CUSTOMS, CHICAGO, DAY5, noVoyage,
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary.eta(), leg2,
                        unknownNextHandlingEvent );

        new RegisterNewDestination( cargo ).to( "CNSHA" );

        assertRouteSpec( CHICAGO,   // Current location
                         SHANGHAI,  // New destination
                         DAY5,      // Last completion time
                         DAY24 );   // Unchanged

        /**
         * Delivery status was updated in {@link InspectCargoInCustoms}
         * We still expect unload in Chicago
         */
        assertDelivery( CUSTOMS, CHICAGO, DAY5, noVoyage,
                        IN_PORT, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }
}
