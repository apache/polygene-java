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
package org.qi4j.sample.dcicargo.sample_b.context.test.handling.inspection.event;

import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.sample.dcicargo.sample_b.bootstrap.test.TestApplication;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.event.InspectReceivedCargo;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.CargoMisdirectedException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.InspectionFailedException;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.CargoAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.HandlingEventAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.NextHandlingEvent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.MISROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.NOT_ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.NOT_RECEIVED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.LOAD;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.RECEIVE;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.UNLOAD;

/**
 * {@link InspectReceivedCargo} tests
 */
public class InspectReceivedCargoTest extends TestApplication
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
        delivery = delivery( TODAY, NOT_RECEIVED, NOT_ROUTED, unknownLeg );
        cargo = CARGOS.createCargo( routeSpec, delivery, "Received_CARGO" );
        trackingId = cargo.trackingId().get();
    }

    @Test
    public void precondition_1_NotHandledBefore()
        throws Exception
    {
        // Handle
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

        // Receive cargo again
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY2, DAY2, trackingId, RECEIVE, HONGKONG, noVoyage );
        thrown.expect( InspectionFailedException.class, "INTERNAL ERROR: Can't receive cargo again" );
        new InspectReceivedCargo( cargo, handlingEvent ).inspect();
    }

    @Test
    public void deviation_2a_NotRouted_MissingItinerary()
        throws Exception
    {
        // Cargo not routed
        cargo.itinerary().set( null );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, NOT_ROUTED, leg1 ) );

        // Receive cargo in Hong Kong (without an itinerary!)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, HONGKONG, noVoyage );

        new InspectReceivedCargo( cargo, handlingEvent ).inspect();

        assertDelivery( RECEIVE, HONGKONG, DAY1, noVoyage,
                        IN_PORT, notArrived,
                        NOT_ROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }

    @Test
    public void deviation_2b_Misrouted_ReceiveLocation_CargoOrigin()
        throws Exception
    {
        // Misroute cargo - assign unsatisfying itinerary not going to Stockholm
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, MISROUTED, unknownLeg ) );

        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );

        // Receive in cargo origin (Hong Kong) having a wrong itinerary
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, HONGKONG, noVoyage );
        new InspectReceivedCargo( cargo, handlingEvent ).inspect();

        // Remains misrouted and directed
        assertDelivery( RECEIVE, HONGKONG, DAY1, noVoyage,
                        IN_PORT, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }

    @Test
    public void deviation_2b_Misrouted_ReceiveLocationOfWrongItinerary_Midpoint()
        throws Exception
    {
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, MISROUTED, leg1 ) );

        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, NEWYORK, noVoyage );

        new InspectReceivedCargo( cargo, handlingEvent ).inspect();

        // Remains misrouted and directed
        assertDelivery( RECEIVE, NEWYORK, DAY1, noVoyage,
                        IN_PORT, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }

    @Test
    public void deviation_2b_Misrouted_ReceiveLocationOfWrongItinerary_Destination()
        throws Exception
    {
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, MISROUTED, unknownLeg ) );

        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );

        // Receipt in cargo destination = no transportation.
        // This must be a mistake. Cargo owner should be notified.
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, STOCKHOLM, noVoyage );
        new InspectReceivedCargo( cargo, handlingEvent ).inspect();

        // Remains misrouted and directed
        assertDelivery( RECEIVE, STOCKHOLM, DAY1, noVoyage,
                        IN_PORT, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }

    @Test
    public void deviation_2b_Misrouted_ReceiveLocationOfWrongItinerary_UnplannedLocation()
        throws Exception
    {
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, MISROUTED, unknownLeg ) );

        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, HANGZHOU, noVoyage );

        new InspectReceivedCargo( cargo, handlingEvent ).inspect();

        // Remains misrouted and directed
        assertDelivery( RECEIVE, HANGZHOU, DAY1, noVoyage,
                        IN_PORT, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }

    @Test
    public void deviation_3a_Misdirected_ReceiveLocationOfCorrectItinerary_Midpoint()
        throws Exception
    {
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, ROUTED, leg1 ) );

        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        ROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );

        // Receive cargo in some location of valid itinerary - should this be accepted?!
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, NEWYORK, noVoyage );
        try
        {
            new InspectReceivedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch( CargoMisdirectedException e )
        {
            assertMessage( e, "MISDIRECTED! Itinerary expected receipt in Hongkong (CNHKG)" );

            // Now routed but misdirected
            assertDelivery( RECEIVE, NEWYORK, DAY1, noVoyage,
                            IN_PORT, notArrived,
                            ROUTED, misdirected, unknownETA, unknownLeg,
                            unknownNextHandlingEvent );
        }
    }

    @Test
    public void deviation_3a_Misdirected_ReceiveLocationOfCorrectItinerary_Destination()
        throws Exception
    {
        // Assign satisfying route going to Stockholm
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, ROUTED, leg1 ) );

        // Receipt in cargo/routeSpec destination = no transportation.
        // This must be a unintended booking. Cargo owner should be notified.
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, STOCKHOLM, noVoyage );
        try
        {
            new InspectReceivedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch( CargoMisdirectedException e )
        {
            assertMessage( e, "MISDIRECTED! Itinerary expected receipt in Hongkong (CNHKG)" );
            assertDelivery( RECEIVE, STOCKHOLM, DAY1, noVoyage,
                            IN_PORT, notArrived,
                            ROUTED, misdirected, unknownETA, unknownLeg,
                            unknownNextHandlingEvent );
        }
    }

    @Test
    public void deviation_3a_Misdirected_UnexpectedReceiveLocation()
        throws Exception
    {
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, ROUTED, leg1 ) );

        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, HANGZHOU, noVoyage );
        thrown.expect( CargoMisdirectedException.class, "MISDIRECTED! Itinerary expected receipt in Hongkong (CNHKG)" );
        new InspectReceivedCargo( cargo, handlingEvent ).inspect();
    }

    @Test
    public void successful_Receipt()
        throws Exception
    {
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, ROUTED, leg1 ) );

        // Receive cargo as planned in origin
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, HONGKONG, noVoyage );
        new InspectReceivedCargo( cargo, handlingEvent ).inspect();

        // Itinerary calculations
        NextHandlingEvent nextLoad = cargo.delivery().get().nextHandlingEvent().get();
        assertThat( nextLoad.location().get(), is( equalTo( itinerary.firstLeg().loadLocation().get() ) ) );
        assertThat( nextLoad.time().get(), is( equalTo( itinerary.firstLeg().loadTime().get() ) ) );
        assertThat( nextLoad.voyage().get(), is( equalTo( itinerary.firstLeg().voyage().get() ) ) );

        assertDelivery( RECEIVE, HONGKONG, DAY1, noVoyage,
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary.eta(), leg1,
                        LOAD, HONGKONG, DAY1, V201 );
    }
}
