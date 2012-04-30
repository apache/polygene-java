package com.marcgrue.dcisample_b.context.test.booking.specification;

import com.marcgrue.dcisample_b.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_b.context.interaction.booking.specification.DeriveUpdatedRouteSpecification;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.*;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.*;

/**
// * {@link com.marcgrue.dcisample_b.context.interaction.booking.routing.RegisterNewDestination} tests
 */
public class DeriveUpdatedRouteSpecTest extends TestApplication
{
    @BeforeClass
    public static void setup() throws Exception
    {
        TestApplication.setup();

        // Create new cargo
        routeSpec = routeSpecFactory.build( HONGKONG, STOCKHOLM, TODAY, deadline = DAY24 );
        delivery = delivery( TODAY, NOT_RECEIVED, ROUTED, unknownLeg );
        cargo = CARGOS.createCargo( routeSpec, delivery, "ABC" );
        cargo.itinerary().set( itinerary );
        trackingId = cargo.trackingId().get();
    }


    @Test
    public void deviation_1a_Destination_changed() throws Exception
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
    public void step_1_Destination_unchanged() throws Exception
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
    public void deviation_2a_NotReceived() throws Exception
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
    public void deviation_2b_OnBoardCarrier() throws Exception
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
    public void step_3_InPort() throws Exception
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
