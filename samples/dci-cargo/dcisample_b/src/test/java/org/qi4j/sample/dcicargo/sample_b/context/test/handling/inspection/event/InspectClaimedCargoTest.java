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
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.event.InspectClaimedCargo;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.CargoAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.HandlingEventAggregateRoot;

import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.MISROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.NOT_ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.CLAIMED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.CLAIM;

/**
 * {@link InspectClaimedCargo} tests
 */
public class InspectClaimedCargoTest extends TestApplication
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
        delivery = delivery( TODAY, IN_PORT, ROUTED, leg1 );
        cargo = CARGOS.createCargo( routeSpec, delivery, "Claimed_CARGO" );
        trackingId = cargo.trackingId().get();
    }

    @Test
    public void deviation_2a_NotRouted_ClaimInFinalDestination()
        throws Exception
    {
        // Cargo not routed
        cargo.itinerary().set( null );
        cargo.delivery().set( delivery( TODAY, IN_PORT, NOT_ROUTED, leg5 ) );

        // Claim in final destination (without itinerary!)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, CLAIM, STOCKHOLM, noVoyage );

        new InspectClaimedCargo( cargo, handlingEvent ).inspect();

        assertDelivery( CLAIM, STOCKHOLM, DAY1, noVoyage,
                        CLAIMED, notArrived,
                        NOT_ROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }

    @Test
    public void deviation_2b_Misrouted_ClaimInDestinationOfWrongItinerary()
        throws Exception
    {
        // Misroute cargo - assign unsatisfying itinerary not going to Stockholm
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, MISROUTED, leg3 ) );

        // Claim in final destination (with wrong itinerary)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY20, DAY20, trackingId, CLAIM, MELBOURNE, noVoyage );
        new InspectClaimedCargo( cargo, handlingEvent ).inspect();

        assertDelivery( CLAIM, MELBOURNE, DAY20, noVoyage,
                        CLAIMED, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }

    @Test
    public void step_2_Routed_ClaimInMidpointLocation()
        throws Exception
    {
        // Assign satisfying route going to Stockholm
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg3 ) );

        // Claim in midpoint before arrival at final destination
        // Should this really be considered misdirected?!
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY9, DAY9, trackingId, CLAIM, DALLAS, noVoyage );
        new InspectClaimedCargo( cargo, handlingEvent ).inspect();

        assertDelivery( CLAIM, DALLAS, DAY9, noVoyage,
                        CLAIMED, notArrived,
                        ROUTED, misdirected, itinerary.eta(), leg3,
                        unknownNextHandlingEvent );
    }

    @Test
    public void step_2_Routed_ClaimInFinalDestination()
        throws Exception
    {
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg5 ) );

        // Claim in final destination
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY24, DAY24, trackingId, CLAIM, STOCKHOLM, noVoyage );
        new InspectClaimedCargo( cargo, handlingEvent ).inspect();

        assertDelivery( CLAIM, STOCKHOLM, DAY24, noVoyage,
                        CLAIMED, notArrived,
                        ROUTED, directed, itinerary.eta(), leg5,
                        unknownNextHandlingEvent );
    }
}
