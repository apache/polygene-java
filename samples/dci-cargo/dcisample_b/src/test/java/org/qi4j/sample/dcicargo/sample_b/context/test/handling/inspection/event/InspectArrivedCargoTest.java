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

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.sample.dcicargo.sample_b.bootstrap.test.TestApplication;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.event.InspectArrivedCargo;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.CargoArrivedException;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.CargoAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.HandlingEventAggregateRoot;

import static org.junit.Assert.fail;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.MISROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.NOT_ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.ONBOARD_CARRIER;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.CLAIM;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.UNLOAD;

/**
 * {@link InspectArrivedCargo} tests
 */
public class InspectArrivedCargoTest extends TestApplication
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
        routeSpec = routeSpecFactory.build( HONGKONG, STOCKHOLM, new Date(), deadline = DAY24 );
        delivery = delivery( TODAY, IN_PORT, ROUTED, leg5 );
        cargo = CARGOS.createCargo( routeSpec, delivery, "Arrived_CARGO" );
        trackingId = cargo.trackingId().get();
    }

    @Test
    public void deviation_2a_NotRouted_MissingItinerary_UnloadedInFinalDestination()
        throws Exception
    {
        // Cargo not routed
        cargo.itinerary().set( null );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, NOT_ROUTED, leg5 ) );

        // Unload in final destination (with no itinerary!)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY23, DAY23, trackingId, UNLOAD, STOCKHOLM, V203 );
        try
        {
            new InspectArrivedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch( CargoArrivedException e )
        {
            assertMessage( e, "Cargo 'Arrived_CARGO' has arrived in destination Stockholm (SESTO)" );

            // An unexpected unload shouldn't be considered an itinerary progress - legIndex stays unchanged
            assertDelivery( UNLOAD, STOCKHOLM, DAY23, V203,
                            IN_PORT, arrived,
                            NOT_ROUTED, directed, unknownETA, unknownLeg,
                            CLAIM, STOCKHOLM, DAY23, noVoyage );
        }
    }

    @Test
    public void deviation_2b_Misrouted_WrongItineraryWithoutCurrentUnloadLocation_UnloadedInFinalDestination()
        throws Exception
    {
        // Misroute cargo - assign unsatisfying itinerary not going to Stockholm
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, MISROUTED, leg5 ) );

        // Unload in final destination (with wrong itinerary)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY23, DAY23, trackingId, UNLOAD, STOCKHOLM, V203 );
        try
        {
            new InspectArrivedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch( CargoArrivedException e )
        {
            assertMessage( e, "Cargo 'Arrived_CARGO' has arrived in destination Stockholm (SESTO)" );
            assertDelivery( UNLOAD, STOCKHOLM, DAY23, V203,
                            IN_PORT, arrived,
                            MISROUTED, directed, unknownETA, unknownLeg,
                            CLAIM, STOCKHOLM, DAY23, noVoyage );
        }
    }

    @Test
    public void success_UnloadInDestination()
        throws Exception
    {
        // Assign satisfying route going to Stockholm
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, ROUTED, leg5 ) );

        // Unload in final destination (with satisfying itinerary)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY23, DAY23, trackingId, UNLOAD, STOCKHOLM, V203 );
        try
        {
            new InspectArrivedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch( CargoArrivedException e )
        {
            assertMessage( e, "Cargo 'Arrived_CARGO' has arrived in destination Stockholm (SESTO)" );
            assertDelivery( UNLOAD, STOCKHOLM, DAY23, V203,
                            IN_PORT, arrived,
                            ROUTED, directed, itinerary.eta(), leg5,
                            CLAIM, STOCKHOLM, DAY23, noVoyage );
        }
    }
}
