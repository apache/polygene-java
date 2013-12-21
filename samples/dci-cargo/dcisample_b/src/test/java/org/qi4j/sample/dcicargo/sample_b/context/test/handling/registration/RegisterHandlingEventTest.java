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
package org.qi4j.sample.dcicargo.sample_b.context.test.handling.registration;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.sample.dcicargo.sample_b.bootstrap.test.TestApplication;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.registration.RegisterHandlingEvent;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.registration.exception.AlreadyClaimedException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.registration.exception.DuplicateEventException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.registration.exception.MissingVoyageNumberException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.registration.exception.UnknownCargoException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.registration.exception.UnknownLocationException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.registration.exception.UnknownVoyageException;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.CargoAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.HandlingEventAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.entity.HandlingEventEntity;
import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;

import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.NOT_RECEIVED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.CLAIM;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.CUSTOMS;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.LOAD;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.RECEIVE;

/**
 * {@link RegisterHandlingEvent} tests
 *
 * FIXME: Every test method call the one above to allow ordered execution, ie. tests are not indepedants !
 */
public class RegisterHandlingEventTest extends TestApplication
{
    private ParsedHandlingEventData parsedEventData;
    private UnitOfWork tempUow;
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
        delivery = delivery( TODAY, NOT_RECEIVED, ROUTED, unknownLeg );
        cargo = CARGOS.createCargo( routeSpec, delivery, "ABC" );
        cargo.itinerary().set( itinerary );
        trackingId = cargo.trackingId().get();
    }

    @Test
    public void deviation_1a_UnknownCargo()
        throws Exception
    {
        parsedEventData = parsedHandlingEventData( DAY1, DAY1, "XXX", RECEIVE, "CNHKG", null );
        thrown.expect( UnknownCargoException.class, "Found no cargo with tracking id 'XXX'." );
        handlingEvent = new RegisterHandlingEvent( parsedEventData ).getEvent();
    }

    @Test
    public void deviation_2a_UnknownUnlocode()
        throws Exception
    {
        deviation_1a_UnknownCargo();

        parsedEventData = parsedHandlingEventData( DAY1, DAY1, "ABC", RECEIVE, "ZZZZZ", null );
        thrown.expect( UnknownLocationException.class, "Found no location with UN locode 'ZZZZZ'." );
        handlingEvent = new RegisterHandlingEvent( parsedEventData ).getEvent();
    }

    @Test
    public void deviation_3a_VoyageNumber_SilentlySkipIfNotRequired()
        throws Exception
    {
        deviation_2a_UnknownUnlocode();

        parsedEventData = parsedHandlingEventData( DAY1, DAY1, "ABC", RECEIVE, "CNHKG", "V201" );
        handlingEvent = new RegisterHandlingEvent( parsedEventData ).getEvent();
    }

    @Test
    public void deviation_3b_VoyageNumber_Missing()
        throws Exception
    {
        deviation_3a_VoyageNumber_SilentlySkipIfNotRequired();

        parsedEventData = parsedHandlingEventData( DAY1, DAY1, "ABC", LOAD, "CNHKG", null );
        thrown.expect( MissingVoyageNumberException.class, "Missing voyage number. Handling event LOAD requires a voyage." );
        handlingEvent = new RegisterHandlingEvent( parsedEventData ).getEvent();
    }

    @Test
    public void deviation_3c_VoyageNumber_Unknown()
        throws Exception
    {
        deviation_3b_VoyageNumber_Missing();

        parsedEventData = parsedHandlingEventData( DAY1, DAY1, "ABC", LOAD, "CNHKG", "V600S" );
        thrown.expect( UnknownVoyageException.class, "Found no voyage with voyage number 'V600S'." );
        handlingEvent = new RegisterHandlingEvent( parsedEventData ).getEvent();
    }

    @Test
    public void deviation_4a_DuplicateEvent_Receive()
        throws Exception
    {
        deviation_3c_VoyageNumber_Unknown();
        UnitOfWork uow = module.currentUnitOfWork();

        // Receive 1st time (store event so that it turns up in query)
        uow.complete();
        tempUow = module.newUnitOfWork();
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, RECEIVE, HONGKONG, noVoyage );
        tempUow.complete();

        // Receive 2nd time
        parsedEventData = parsedHandlingEventData( DAY2, DAY2, "ABC", RECEIVE, "CNHKG", null );
        thrown.expect( DuplicateEventException.class, "Cargo can't be received more than once" );
        handlingEvent = new RegisterHandlingEvent( parsedEventData ).getEvent();
    }

    @Test
    public void deviation_4a_DuplicateEvent_Customs()
        throws Exception
    {
        deviation_4a_DuplicateEvent_Receive();
        UnitOfWork uow = module.currentUnitOfWork();

        uow.complete();
        // In customs 1st time
        tempUow = module.newUnitOfWork();
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, CUSTOMS, HONGKONG, noVoyage );
        tempUow.complete();

        // In customs 2nd time
        parsedEventData = parsedHandlingEventData( DAY2, DAY2, "ABC", CUSTOMS, "CNHKG", null );
        thrown.expect( DuplicateEventException.class, "Cargo can't be in customs more than once" );
        handlingEvent = new RegisterHandlingEvent( parsedEventData ).getEvent();
    }

    @Test
    public void deviation_4a_DuplicateEvent_Claim()
        throws Exception
    {
        deviation_4a_DuplicateEvent_Customs();
        UnitOfWork uow = module.currentUnitOfWork();

        uow.complete();
        // Claimed 1st time
        tempUow = module.newUnitOfWork();
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, CLAIM, HONGKONG, noVoyage );
        tempUow.complete();

        // Claimed 2nd time
        parsedEventData = parsedHandlingEventData( DAY2, DAY2, "ABC", CLAIM, "CNHKG", null );
        thrown.expect( DuplicateEventException.class, "Cargo can't be claimed more than once" );
        new RegisterHandlingEvent( parsedEventData ).getEvent();
    }

    @Test
    public void deviation_5a_NoHandlingAfterClaim()
        throws Exception
    {
        deviation_4a_DuplicateEvent_Claim();

        // Try loading (saved claim event in previous test will prevent this)
        parsedEventData = parsedHandlingEventData( DAY2, DAY2, "ABC", LOAD, "CNHKG", "V201" );
        thrown.expect( AlreadyClaimedException.class, "LOAD handling event can't be registered after cargo has been claimed" );
        new RegisterHandlingEvent( parsedEventData ).getEvent();
    }

    @Test
    public void successfull_Registration()
        throws Exception
    {
        deviation_5a_NoHandlingAfterClaim();

        // Delete handling events from memory
        tempUow = module.newUnitOfWork();
        Query<HandlingEventEntity> events = tempUow.newQuery( module.newQueryBuilder( HandlingEventEntity.class ) );
        for( HandlingEvent event : events )
        {
            tempUow.remove( event );
        }
        tempUow.complete();

        cargo.delivery().set( delivery( null, NOT_RECEIVED, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg1,
                                        nextHandlingEvent( RECEIVE, HONGKONG, DAY1, noVoyage ) ) );

        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        ROUTED, directed, itinerary.eta(), leg1,
                        RECEIVE, HONGKONG, DAY1, noVoyage );

        parsedEventData = parsedHandlingEventData( DAY1, DAY1, "ABC", RECEIVE, "CNHKG", null );
        handlingEvent = new RegisterHandlingEvent( parsedEventData ).getEvent();

        cargo.delivery().set( delivery( handlingEvent, IN_PORT, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg1,
                                        nextHandlingEvent( LOAD, HONGKONG, DAY1, V201 ) ) );

        assertDelivery( RECEIVE, HONGKONG, DAY1, noVoyage,  // Handling event has been registered
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary.eta(), leg1,
                        LOAD, HONGKONG, DAY1, V201 );
    }
}