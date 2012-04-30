package com.marcgrue.dcisample_b.context.test.booking.routing;

import com.marcgrue.dcisample_b.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_b.context.interaction.booking.exception.ChangeDestinationException;
import com.marcgrue.dcisample_b.context.interaction.booking.routing.RegisterNewDestination;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.event.*;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.CargoMisroutedException;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.*;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.*;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.*;
import static org.junit.Assert.fail;

/**
 * {@link RegisterNewDestination} tests
 */
public class RegisterNewDestinationTest extends TestApplication
{
    @BeforeClass
    public static void setup() throws Exception
    {
        TestApplication.setup();

        // Create new cargo
        routeSpec = routeSpecFactory.build( HONGKONG, STOCKHOLM, TODAY, deadline = DAY24 );
        delivery = delivery( TODAY, NOT_RECEIVED, NOT_ROUTED, leg1 );
        cargo = CARGOS.createCargo( routeSpec, delivery, "ABC" );
        trackingId = cargo.trackingId().get();
        delivery = cargo.delivery().get();
    }

    @Test
    public void precondition_x1_CannotChangeDestinationOfClaimedCargo() throws Exception
    {
        cargo.delivery().set( delivery( DAY1, CLAIMED, ROUTED, leg1 ) );
        thrown.expect( ChangeDestinationException.class, "Can't change destination of claimed cargo" );
        new RegisterNewDestination( cargo ).to( "USCHI" );
    }

    @Test
    public void deviation_1a_UnrecognizedLocation() throws Exception
    {
        cargo.delivery().set( delivery( DAY1, IN_PORT, ROUTED, leg1 ) );
        thrown.expect( ChangeDestinationException.class, "Didn't recognize location 'XXXXX'" );
        new RegisterNewDestination( cargo ).to( "XXXXX" );
    }

    @Test
    public void deviation_1b_NewDestinationSameAsOldDestination() throws Exception
    {
        cargo.delivery().set( delivery( DAY1, IN_PORT, ROUTED, leg1 ) );
        thrown.expect( ChangeDestinationException.class, "New destination is same as old destination." );
        new RegisterNewDestination( cargo ).to( "SESTO" );
    }

    @Test
    public void step_2_NotRouted() throws Exception
    {
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
    public void step_2_NotReceived() throws Exception
    {
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
    public void step_2_Received() throws Exception
    {
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
    public void deviation_2a_OnBoardCarrier() throws Exception
    {
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
        catch (CargoMisroutedException e)
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
    public void deviation_2b_InPort_Unloaded() throws Exception
    {
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
        catch (CargoMisroutedException e)
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
    public void deviation_2b_InPort_InCustoms() throws Exception
    {
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
