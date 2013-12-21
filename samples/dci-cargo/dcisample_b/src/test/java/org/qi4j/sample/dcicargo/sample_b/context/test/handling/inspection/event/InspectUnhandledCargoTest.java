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
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.event.InspectUnhandledCargo;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.InspectionFailedException;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.CargoAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.HandlingEventAggregateRoot;

import static org.junit.Assert.fail;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.MISROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.NOT_ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.NOT_RECEIVED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.RECEIVE;

/**
 * {@link InspectUnhandledCargo} tests
 */
public class InspectUnhandledCargoTest extends TestApplication
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
        delivery = delivery( TODAY, NOT_RECEIVED, NOT_ROUTED, leg1 );
        cargo = CARGOS.createCargo( routeSpec, delivery, "Claimed_CARGO" );
        trackingId = cargo.trackingId().get();
    }

    @Test
    public void precondition_CannotInspectUnloadInDestinationHere()
        throws Exception
    {
        // Can't inspect handled cargo here...
        cargo.itinerary().set( itinerary );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, HONGKONG, noVoyage );
        cargo.delivery().set( delivery( handlingEvent, IN_PORT, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg1, unknownNextHandlingEvent ) );
        try
        {
            new InspectUnhandledCargo( cargo ).inspect();
            fail();
        }
        catch( InspectionFailedException e )
        {
            assertMessage( e, "INTERNAL ERROR: Can only inspect unhandled cargo" );
        }
    }

    @Test
    public void deviation_2a_NotRouted()
        throws Exception
    {
        // Cargo not routed
        cargo.itinerary().set( null );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, NOT_ROUTED, leg1 ) );

        new InspectUnhandledCargo( cargo ).inspect();

        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        NOT_ROUTED, directed, unknownETA, unknownLeg,
                        RECEIVE, HONGKONG, noSpecificDate, noVoyage );
    }

    @Test
    public void deviation_2b_Misrouted()
        throws Exception
    {
        // Misroute cargo - assign unsatisfying itinerary not going to Stockholm
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, MISROUTED, leg1 ) );

        new InspectUnhandledCargo( cargo ).inspect();

        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        RECEIVE, HONGKONG, noSpecificDate, noVoyage );
    }

    @Test
    public void step_2_Routed()
        throws Exception
    {
        // Assign satisfying route going to Stockholm
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, ROUTED, leg1 ) );

        new InspectUnhandledCargo( cargo ).inspect();

        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        ROUTED, directed, itinerary.eta(), leg1,
                        RECEIVE, HONGKONG, noSpecificDate, noVoyage );
    }
}
