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

import java.util.Date;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.sample.dcicargo.sample_b.bootstrap.test.TestApplication;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.booking.exception.RoutingException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.booking.exception.UnsatisfyingRouteException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.booking.routing.AssignCargoToRoute;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;

import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.NOT_ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.*;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.*;

/**
 * {@link AssignCargoToRoute} tests
 *
 * FIXME: Every test method call the one above to allow ordered execution, ie. tests are not indepedants !
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
        precondition_x1_CannotReRouteClaimedCargo();

        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, NOT_ROUTED, unknownLeg ) );
        thrown.expect( UnsatisfyingRouteException.class, "Route specification was not satisfied with itinerary" );
        new AssignCargoToRoute( cargo, wrongItinerary ).assign();
    }

    @Test
    public void deviation_3a_Routing_UnhandledCargo() throws Exception
    {
        deviation_1a_UnsatisfyingItinerary();

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
        deviation_3a_Routing_UnhandledCargo();

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
        deviation_3b_ReRouting_OnBoard();

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
        deviation_3c_ReRouting_InPort_Received();

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
        deviation_3c_ReRouting_InPort_Unloaded();

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
