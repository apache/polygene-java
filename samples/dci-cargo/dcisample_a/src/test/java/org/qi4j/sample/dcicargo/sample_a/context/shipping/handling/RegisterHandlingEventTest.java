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

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.sample.dcicargo.sample_a.bootstrap.test.TestApplication;
import org.qi4j.sample.dcicargo.sample_a.context.shipping.booking.BookNewCargo;
import org.qi4j.sample.dcicargo.sample_a.data.entity.CargosEntity;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.Cargo;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.Cargos;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.TrackingId;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.delivery.Delivery;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.delivery.RoutingStatus;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.delivery.TransportStatus;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.itinerary.Itinerary;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.location.Location;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage.Voyage;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.qi4j.sample.dcicargo.sample_a.data.shipping.handling.HandlingEventType.UNLOAD;

/**
 * Testing the Register Handling Event use case.
 *
 * Test method names describe the test purpose. The prefix refers to the step in the use case.
 */
public class RegisterHandlingEventTest
      extends TestApplication
{
    Date time;
    String trackId;
    String msg;
    private Date arrival;
    private Cargo cargo;
    private TrackingId trackingId;
    private Cargos CARGOS;
    private Location HONGKONG;
    private Location STOCKHOLM;
    private Location NEWYORK;
    private Location DALLAS;
    private Voyage V100S;
    private Voyage V200T;
    private Voyage V300A;

    @Before
    public void beforeEachTest() throws Exception {
        UnitOfWork uow = module.currentUnitOfWork();
        CARGOS = uow.get(Cargos.class,  CargosEntity.CARGOS_ID );
        HONGKONG = uow.get( Location.class, CNHKG.code().get() );
        STOCKHOLM = uow.get( Location.class, SESTO.code().get() );
        NEWYORK = uow.get( Location.class, USNYC.code().get() );
        DALLAS = uow.get( Location.class, USDAL.code().get() );
        V100S = uow.get( Voyage.class, "V100S" );
        V200T = uow.get( Voyage.class, "V200T" );
        V300A = uow.get( Voyage.class, "V300A" );
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, day( 17 ) ).createCargo( "ABC" );
        cargo = uow.get( Cargo.class, trackingId.id().get() );
        Itinerary itinerary = itinerary(
              leg( V100S, HONGKONG, NEWYORK, day( 1 ), day( 8 ) ),
              leg( V200T, NEWYORK, DALLAS, day( 9 ), day( 12 ) ),
              leg( V300A, DALLAS, STOCKHOLM, day( 13 ),
                   arrival = day( 16 ) )
        );
        new BookNewCargo( cargo, itinerary ).assignCargoToRoute();
        time = day( 1 );
        trackId = trackingId.id().get();
    }

    // INPUT EXISTENCE VALIDATION ==================================================================

    @Test
    public void deviation_2a_MissingRegistrationTime() throws Exception
    {
        msg = register( null, time, trackId, "RECEIVE", "CNHKG", null );
        assertThat( msg, is( equalTo( "Registration time was null. All parameters have to be passed." ) ) );
    }
    @Test
    public void deviation_2a_MissingCompletionTime() throws Exception
    {
        msg = register( time, null, trackId, "RECEIVE", "CNHKG", null );
        assertThat( msg, is( equalTo( "Completion time was null. All parameters have to be passed." ) ) );
    }

    @Test
    public void deviation_2a_MissingTrackingId() throws Exception
    {
        msg = register( time, time, null, "RECEIVE", "CNHKG", null );
        assertThat( msg, is( equalTo( "Tracking id was null. All parameters have to be passed." ) ) );
    }

    @Test
    public void deviation_2a_EmptyTrackingId() throws Exception
    {
        msg = register( time, time, "", "RECEIVE", "CNHKG", null );
        assertThat( msg, is( equalTo( "Tracking id cannot be empty." ) ) );
    }

    @Test
    public void deviation_2a_MissingEventType() throws Exception
    {
        msg = register( time, time, trackId, null, "CNHKG", null );
        assertThat( msg, is( equalTo( "Event type was null. All parameters have to be passed." ) ) );
    }

    @Test
    public void deviation_2a_EmptyEventType() throws Exception
    {
        msg = register( time, time, trackId, "", "CNHKG", null );
        assertThat( msg, is( equalTo( "Event type cannot be empty." ) ) );
    }

    @Test
    public void deviation_2a_MissingUnlocode() throws Exception
    {
        msg = register( time, time, trackId, "RECEIVE", null, null );
        assertThat( msg, is( equalTo( "UnLocode was null. All parameters have to be passed." ) ) );
    }

    @Test
    public void deviation_2a_EmptyUnlocode() throws Exception
    {
        msg = register( time, time, trackId, "RECEIVE", "", null );
        assertThat( msg, is( equalTo( "UnLocode cannot be empty." ) ) );
    }

    @Test
    public void step_2_CompleteData__Receive_in_Hong_Kong() throws Exception
    {
        new RegisterHandlingEvent( time, time, trackId, "RECEIVE", "CNHKG", null ).register();
    }


    // INPUT VALIDATION ==================================================================

    @Test
    public void deviation_3a_HandlingTypeNotRecognized() throws Exception
    {
        msg = register( time, time, trackId, "RECEIPT", "CNHKG", null );
        assertThat( msg, is( equalTo(
              "'RECEIPT' is not a valid handling event type. Valid types are: [RECEIVE, LOAD, UNLOAD, CUSTOMS, CLAIM]" ) ) );
    }

    @Test
    public void deviation_3b_NoCargoWithTrackingId() throws Exception
    {
        msg = register( time, time, "XXX", "RECEIVE", "CNHKG", null );
        assertThat( msg, is( equalTo( "Found no cargo with tracking id 'XXX'." ) ) );
    }

    @Test
    public void deviation_3c_CargoNotRoutedYet() throws Exception
    {
        TrackingId nonRoutedTrackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, day( 17 ) ).createCargo( "NonRoutedCargo" );
        String nonRouted = nonRoutedTrackingId.id().get();

        msg = register( time, time, nonRouted, "RECEIVE", "CNHKG", null );
        assertThat( msg, is( equalTo( "Can't create handling event for non-routed cargo '" + nonRouted + "'." ) ) );
    }

    @Test
    public void deviation_3d_NoLocationWithUnlocode() throws Exception
    {
        msg = register( time, time, trackId, "RECEIVE", "ZZZZZ", null );
        assertThat( msg, is( equalTo( "Unknown location: ZZZZZ" ) ) );
    }

    @Test
    public void deviation_3e_1a_MissingVoyageNumber() throws Exception
    {
        msg = register( time, time, trackId, "LOAD", "CNHKG", null );
        assertThat( msg, is( equalTo( "Handling event LOAD requires a voyage. No voyage number submitted." ) ) );
    }

    @Test
    public void deviation_3e_1b_MissingVoyage() throws Exception
    {
        msg = register( time, time, trackId, "LOAD", "CNHKG", "V600S" );
        assertThat( msg, is( equalTo( "Found no voyage with voyage number 'V600S'." ) ) );
    }

    @Test
    public void deviation_3f_SkipVoyageNumberSilentlyWhenProhibited() throws Exception
    {
        new RegisterHandlingEvent( time, time, trackId, "RECEIVE", "CNHKG", "V100S" ).register();
        assertThat( cargo.delivery().get().currentVoyage().get(), is( equalTo( null ) ) );
    }

    @Test
    public void step_3_to_5_ValidRegistration__Load_in_Hong_Kong() throws Exception
    {
        new RegisterHandlingEvent( time, time, trackId, "LOAD", "CNHKG", "V100S" ).register();

        Delivery delivery = cargo.delivery().get();
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.ONBOARD_CARRIER ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().handlingEventType().get(), is( equalTo( UNLOAD ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().location().get(), is( equalTo( NEWYORK ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().voyage().get(), is( equalTo( V100S ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( HONGKONG ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( V100S ) ) );
        assertThat( delivery.eta().get(), is( equalTo( arrival ) ) );
        assertThat( delivery.isMisdirected().get(), is( equalTo( false ) ) );
        assertThat( delivery.isUnloadedAtDestination().get(), is( equalTo( false ) ) );
    }


    private String register( Date registrationTime,
                             Date completionTime,
                             String trackingIdString,
                             String eventTypeString,
                             String unLocodeString,
                             String voyageNumberString ) throws Exception
    {
        try
        {
            new RegisterHandlingEvent( registrationTime,
                                       completionTime,
                                       trackingIdString,
                                       eventTypeString,
                                       unLocodeString,
                                       voyageNumberString ).register();
        }
        catch (IllegalArgumentException e)
        {
            return e.getMessage();
        }
        throw new Exception( "Unexpected exception." );
    }
}