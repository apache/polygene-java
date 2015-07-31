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
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.event.InspectCargoInCustoms;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.InspectionFailedException;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.CargoAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.HandlingEventAggregateRoot;

import static org.junit.Assert.fail;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.MISROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.NOT_ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.ONBOARD_CARRIER;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.CUSTOMS;

/**
 * {@link InspectCargoInCustoms} tests
 */
public class InspectCargoInCustomsTest extends TestApplication
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
        cargo = CARGOS.createCargo( routeSpec, delivery, "CARGO_in_customs" );
        trackingId = cargo.trackingId().get();
    }

    @Test
    public void precondition_CustomsHandlingNotOnBoardCarrier()
        throws Exception
    {
        cargo.itinerary().set( itinerary );

        // No customs handling on board a carrier...
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, CUSTOMS, STOCKHOLM, noVoyage );
        cargo.delivery().set( delivery( handlingEvent, ONBOARD_CARRIER, notArrived,
                                        ROUTED, directed, unknownETA, unknownLeg,
                                        unknownNextHandlingEvent ) );
        try
        {
            new InspectCargoInCustoms( cargo, handlingEvent ).inspect();
            fail();
        }
        catch( InspectionFailedException e )
        {
            assertMessage( e, "INTERNAL ERROR: Cannot handle cargo in customs on board a carrier." );
        }
    }

    @Test
    public void deviation_2a_NotRouted_CustomsLocation_FinalDestination()
        throws Exception
    {
        // Cargo not routed
        cargo.itinerary().set( null );
        cargo.delivery().set( delivery( TODAY, IN_PORT, NOT_ROUTED, leg5 ) );

        // Handle in customs (without itinerary!)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, CUSTOMS, STOCKHOLM, noVoyage );
        new InspectCargoInCustoms( cargo, handlingEvent ).inspect();

        assertDelivery( CUSTOMS, STOCKHOLM, DAY1, noVoyage,
                        IN_PORT, notArrived,
                        NOT_ROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }

    @Test
    public void deviation_2b_Misrouted_CustomsLocation_DestinationOfWrongItinerary()
        throws Exception
    {
        // Misroute cargo - assign unsatisfying itinerary not going to Stockholm
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, MISROUTED, leg3 ) );

        // Handle in customs (with wrong itinerary)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY20, DAY20, trackingId, CUSTOMS, MELBOURNE, noVoyage );
        new InspectCargoInCustoms( cargo, handlingEvent ).inspect();

        assertDelivery( CUSTOMS, MELBOURNE, DAY20, noVoyage,
                        IN_PORT, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }

    @Test
    public void step_2_Routed_CustomsLocation_FinalDestination()
        throws Exception
    {
        // Assign satisfying route going to Stockholm
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, MISROUTED, leg5 ) );

        // Handle in customs (without itinerary!)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY24, DAY24, trackingId, CUSTOMS, STOCKHOLM, noVoyage );
        new InspectCargoInCustoms( cargo, handlingEvent ).inspect();

        assertDelivery( CUSTOMS, STOCKHOLM, DAY24, noVoyage,
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary.eta(), leg5,
                        unknownNextHandlingEvent );
    }
}
