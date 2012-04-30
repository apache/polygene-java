package com.marcgrue.dcisample_b.context.test.booking.routing;

import com.marcgrue.dcisample_b.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_b.context.interaction.booking.exception.RoutingException;
import com.marcgrue.dcisample_b.context.interaction.booking.exception.UnsatisfyingRouteException;
import com.marcgrue.dcisample_b.context.interaction.booking.routing.AssignCargoToRoute;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.NOT_ROUTED;
import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.*;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.*;

/**
 * {@link AssignCargoToRoute} tests
 */
public class AssignCargoToRouteTest extends TestApplication
{
    static Itinerary itinerary2;

    @BeforeClass
    public static void setup() throws Exception
    {
        TestApplication.setup();

        // Create new cargo
        routeSpec = routeSpecFactory.build( HONGKONG, STOCKHOLM, new Date(), deadline = DAY24 );
        delivery = delivery( TODAY, NOT_RECEIVED, NOT_ROUTED, unknownLeg );
        cargo = CARGOS.createCargo( routeSpec, delivery, "ABC" );
        trackingId = cargo.trackingId().get();
        delivery = cargo.delivery().get();
    }

    @Test
    public void precondition_x1_CannotReRouteClaimedCargo() throws Exception
    {
        cargo.delivery().set( delivery( TODAY, CLAIMED, ROUTED, unknownLeg ) );
        thrown.expect( RoutingException.class, "Can't re-route claimed cargo" );
        new AssignCargoToRoute( cargo, itinerary ).assign();
    }

    @Test
    public void deviation_1a_UnsatisfyingItinerary() throws Exception
    {
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, NOT_ROUTED, unknownLeg ) );
        thrown.expect( UnsatisfyingRouteException.class, "Route specification was not satisfied with itinerary" );
        new AssignCargoToRoute( cargo, wrongItinerary ).assign();
    }

    @Test
    public void deviation_3a_Routing_UnhandledCargo() throws Exception
    {
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, NOT_ROUTED, unknownLeg ) );
        new AssignCargoToRoute( cargo, itinerary ).assign();
        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        ROUTED, directed, itinerary.eta(), leg1,
                        RECEIVE, HONGKONG, noSpecificDate, noVoyage );
    }

    @Test
    public void deviation_3b_ReRouting_OnBoard() throws Exception
    {
        // Load cargo
        cargo.itinerary().set( itinerary );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY5, DAY5, trackingId, LOAD, CHICAGO, V201 );
        cargo.delivery().set( delivery( handlingEvent, ONBOARD_CARRIER, notArrived,
                                        ROUTED, directed, DAY23, leg2,
                                        nextHandlingEvent( UNLOAD, NEWYORK, DAY6, V201 ) ) );

        // New itinerary with arrival location of current carrier movement
        // Earliest departure date is after carrier arrival
        itinerary2 = itinerary( leg( V202, NEWYORK, STOCKHOLM, DAY8, DAY17 ) );

        // Re-route cargo while on board a carrier
        new AssignCargoToRoute( cargo, itinerary2 ).assign();
        assertDelivery( LOAD, CHICAGO, DAY5, V201,
                        ONBOARD_CARRIER, notArrived,
                        ROUTED, directed, itinerary2.eta(), leg1,
                        UNLOAD, NEWYORK, DAY6, V201 ); // from old itinerary!
    }

    @Test
    public void deviation_3c_ReRouting_InPort_Received() throws Exception
    {
        // Receive cargo
        cargo.itinerary().set( itinerary );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, HONGKONG, noVoyage );
        cargo.delivery().set( delivery( handlingEvent, IN_PORT, notArrived,
                                        ROUTED, directed, unknownETA, unknownLeg,
                                        nextHandlingEvent( LOAD, HONGKONG, DAY1, V201 )  ) );

        // New itinerary going from current port
        itinerary2 = itinerary( leg( V202, HONGKONG, STOCKHOLM, DAY3, DAY17 ) );

        // Re-route cargo after receipt in port
        new AssignCargoToRoute( cargo, itinerary2 ).assign();
        assertDelivery( RECEIVE, HONGKONG, DAY1, noVoyage,
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary2.eta(), leg1,
                        LOAD, HONGKONG, DAY3, V202 ); // from new itinerary!
    }

    @Test
    public void deviation_3c_ReRouting_InPort_Unloaded() throws Exception
    {
        // Unload cargo
        cargo.itinerary().set( itinerary );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY5, DAY5, trackingId, UNLOAD, CHICAGO, V201 );
        cargo.delivery().set( delivery( handlingEvent, IN_PORT, notArrived,
                                        ROUTED, directed, unknownETA, leg2,
                                        nextHandlingEvent( UNLOAD, NEWYORK, DAY6, V201 )  ) );

        // Re-route cargo unloaded in port
        itinerary2 = itinerary( leg( V202, CHICAGO, STOCKHOLM, DAY6, DAY19 ) );
        new AssignCargoToRoute( cargo, itinerary2 ).assign();
        assertDelivery( UNLOAD, CHICAGO, DAY5, V201,
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary2.eta(), leg1,
                        LOAD, CHICAGO, DAY6, V202 );
    }

    @Test
    public void deviation_3c_ReRouting_InPort_InCustoms() throws Exception
    {
        // Receive cargo
        cargo.itinerary().set( itinerary );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY6, DAY6, trackingId, CUSTOMS, NEWYORK, noVoyage );
        cargo.delivery().set( delivery( handlingEvent, IN_PORT, notArrived,
                                        ROUTED, directed, unknownETA, leg3,
                                        unknownNextHandlingEvent ) );

        // Re-route cargo while in customs
        itinerary2 = itinerary( leg( V202, NEWYORK, STOCKHOLM, DAY8, DAY18 ) );
        new AssignCargoToRoute( cargo, itinerary2 ).assign();
        assertDelivery( CUSTOMS, NEWYORK, DAY6, noVoyage,
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary2.eta(), leg1,
                        LOAD, NEWYORK, DAY8, V202 );
    }
}
