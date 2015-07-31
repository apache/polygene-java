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
package org.qi4j.sample.dcicargo.sample_a.context.shipping.handling;

import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.sample.dcicargo.sample_a.bootstrap.test.TestApplication;
import org.qi4j.sample.dcicargo.sample_a.context.shipping.booking.BookNewCargo;
import org.qi4j.sample.dcicargo.sample_a.context.shipping.booking.BuildDeliverySnapshot;
import org.qi4j.sample.dcicargo.sample_a.data.entity.CargosEntity;
import org.qi4j.sample.dcicargo.sample_a.data.entity.HandlingEventsEntity;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.Cargo;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.Cargos;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.TrackingId;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.delivery.Delivery;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.handling.HandlingEvent;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.handling.HandlingEventType;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.itinerary.Itinerary;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.location.Location;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage.Voyage;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Javadoc
 *
 * Test method names describe the test purpose. The prefix refers to the step in the use case.
 */
public class InspectCargoTest
    extends TestApplication
{
    private Cargo cargo;
    private TrackingId trackingId;
    private Location STOCKHOLM;
    private Location DALLAS;
    private Location SHANGHAI;
    private Voyage V200T;
    private Voyage V300A;
    private HandlingEvent handlingEvent;

    @Before
    public void beforeEachTest()
        throws Exception
    {
        UnitOfWork uow = module.currentUnitOfWork();
        Cargos CARGOS = uow.get( Cargos.class, CargosEntity.CARGOS_ID );
        Location HONGKONG = uow.get( Location.class, CNHKG.code().get() );
        SHANGHAI = uow.get( Location.class, CNSHA.code().get() );
        STOCKHOLM = uow.get( Location.class, SESTO.code().get() );
        Location NEWYORK = uow.get( Location.class, USNYC.code().get() );
        DALLAS = uow.get( Location.class, USDAL.code().get() );
        Voyage V100S = uow.get( Voyage.class, "V100S" );
        V200T = uow.get( Voyage.class, "V200T" );
        V300A = uow.get( Voyage.class, "V300A" );

        // Create cargo
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, day( 17 ) ).createCargo( "ABC" );
        cargo = uow.get( Cargo.class, trackingId.id().get() );
        Itinerary itinerary = itinerary(
            leg( V100S, HONGKONG, NEWYORK, day( 1 ), day( 8 ) ),
            leg( V200T, NEWYORK, DALLAS, day( 9 ), day( 12 ) ),
            leg( V300A, DALLAS, STOCKHOLM, day( 13 ), day( 16 ) )
        );

        // Route cargo
        new BookNewCargo( cargo, itinerary ).assignCargoToRoute();
    }

    @Test
    public void deviation_3a_CargoIsMisdirected()
        throws Exception
    {
        // Create misdirected handling event for cargo (receipt in Shanghai is unexpected)
        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 0 ), day( 0 ), trackingId, HandlingEventType.RECEIVE, SHANGHAI, null );
        Delivery delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();
        cargo.delivery().set( delivery );
        assertThat( cargo.delivery().get().isMisdirected().get(), is( equalTo( true ) ) );

        logger.info( "  Handling cargo 'ABC' (misdirected):" );
        new InspectCargo( handlingEvent ).inspect();

        // Assert that notification of misdirection has been sent (see console output).
    }

    @Test
    public void deviation_3b_CargoHasArrived()
        throws Exception
    {
        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 15 ), day( 15 ), trackingId, HandlingEventType.UNLOAD, STOCKHOLM, V300A );
        Delivery delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();
        cargo.delivery().set( delivery );
        assertThat( cargo.delivery().get().isUnloadedAtDestination().get(), is( equalTo( true ) ) );

        logger.info( "  Handling cargo 'ABC' (arrived):" );
        new InspectCargo( handlingEvent ).inspect();

        // Assert that notification of  arrival has been sent (see console output).
    }

    @Test
    public void step_3_CargoIsCorrectlyInTransit()
        throws Exception
    {
        logger.info( "  Handling cargo 'ABC' (unloaded in Dallas):" );
        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 12 ), day( 12 ), trackingId, HandlingEventType.UNLOAD, DALLAS, V200T );
        cargo.delivery().set( new BuildDeliverySnapshot( cargo, handlingEvent ).get() );
        assertThat( cargo.delivery().get().isMisdirected().get(), is( equalTo( false ) ) );
        new InspectCargo( handlingEvent ).inspect();

        logger.info( "  Cargo was correctly directed." );
    }
}