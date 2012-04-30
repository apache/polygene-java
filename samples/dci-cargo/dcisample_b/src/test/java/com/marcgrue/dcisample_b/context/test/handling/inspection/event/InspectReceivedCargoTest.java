package com.marcgrue.dcisample_b.context.test.handling.inspection.event;

import com.marcgrue.dcisample_b.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.event.InspectReceivedCargo;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.CargoMisdirectedException;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.InspectionFailedException;
import com.marcgrue.dcisample_b.data.structure.delivery.NextHandlingEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.*;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.NOT_RECEIVED;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link InspectReceivedCargo} tests
 */
public class InspectReceivedCargoTest extends TestApplication
{
    @BeforeClass
    public static void setup() throws Exception
    {
        TestApplication.setup();

        // Create new cargo
        routeSpec = routeSpecFactory.build( HONGKONG, STOCKHOLM, TODAY, deadline = DAY24 );
        delivery = delivery( TODAY, NOT_RECEIVED, NOT_ROUTED, unknownLeg );
        cargo = CARGOS.createCargo( routeSpec, delivery, "Received_CARGO" );
        trackingId = cargo.trackingId().get();
    }

    @Test
    public void precondition_1_NotHandledBefore() throws Exception
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
    public void deviation_2a_NotRouted_MissingItinerary() throws Exception
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
    public void deviation_2b_Misrouted_ReceiveLocation_CargoOrigin() throws Exception
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
    public void deviation_2b_Misrouted_ReceiveLocationOfWrongItinerary_Midpoint() throws Exception
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
    public void deviation_2b_Misrouted_ReceiveLocationOfWrongItinerary_Destination() throws Exception
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
    public void deviation_2b_Misrouted_ReceiveLocationOfWrongItinerary_UnplannedLocation() throws Exception
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
    public void deviation_3a_Misdirected_ReceiveLocationOfCorrectItinerary_Midpoint() throws Exception
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
        catch (CargoMisdirectedException e)
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
    public void deviation_3a_Misdirected_ReceiveLocationOfCorrectItinerary_Destination() throws Exception
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
        catch (CargoMisdirectedException e)
        {
            assertMessage( e, "MISDIRECTED! Itinerary expected receipt in Hongkong (CNHKG)" );
            assertDelivery( RECEIVE, STOCKHOLM, DAY1, noVoyage,
                            IN_PORT, notArrived,
                            ROUTED, misdirected, unknownETA, unknownLeg,
                            unknownNextHandlingEvent );
        }
    }

    @Test
    public void deviation_3a_Misdirected_UnexpectedReceiveLocation() throws Exception
    {
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, ROUTED, leg1 ) );

        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, HANGZHOU, noVoyage );
        thrown.expect( CargoMisdirectedException.class, "MISDIRECTED! Itinerary expected receipt in Hongkong (CNHKG)" );
        new InspectReceivedCargo( cargo, handlingEvent ).inspect();
    }

    @Test
    public void successful_Receipt() throws Exception
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
