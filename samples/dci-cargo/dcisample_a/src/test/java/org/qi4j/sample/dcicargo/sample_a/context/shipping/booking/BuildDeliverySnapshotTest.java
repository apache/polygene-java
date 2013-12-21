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
package org.qi4j.sample.dcicargo.sample_a.context.shipping.booking;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.sample.dcicargo.sample_a.bootstrap.test.TestApplication;
import org.qi4j.sample.dcicargo.sample_a.data.entity.CargosEntity;
import org.qi4j.sample.dcicargo.sample_a.data.entity.HandlingEventsEntity;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.Cargo;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.Cargos;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.RouteSpecification;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.TrackingId;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.delivery.Delivery;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.delivery.RoutingStatus;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.delivery.TransportStatus;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.handling.HandlingEvent;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.handling.HandlingEventType;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.itinerary.Itinerary;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.location.Location;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage.Voyage;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.qi4j.sample.dcicargo.sample_a.data.shipping.handling.HandlingEventType.*;

/**
 * Tests of the Build Delivery Snapshot subfunction use case.
 *
 * All deviations of the use case are tested one by one. The business rules are therefore enforcing the
 * structure for the test suite and not arbitrary ideas of the programmer what to test. If the business
 * analyst haven't foreseen a deviation in the use case, it's his responsibility that it's not tested.
 *
 * Test method names describe the test purpose. The prefix refers to the step in the use case.
 *
 * FIXME: Test methods call each other to allow ordered execution, ie. tests are not indepedants !
 */
public class BuildDeliverySnapshotTest
      extends TestApplication
{
    final Date TODAY = new Date();

    private Location HONGKONG;
    private Location STOCKHOLM;
    private Location NEWYORK;
    private Location DALLAS;
    private Location HANGZHOU;
    private Location HELSINKI;
    private Location TOKYO;
    private Location HAMBURG;
    private Voyage V100S;
    private Voyage V200T;
    private Voyage V300A;
    private Voyage V400S;
    private Voyage V500S;
    private Location SHANGHAI;
    private TrackingId trackingId;
    private Cargo cargo;
    private Itinerary itinerary;

    @Before
    public void prepareTest()
        throws Exception
    {
        super.prepareTest();
        UnitOfWork uow = module.currentUnitOfWork();
        HONGKONG = uow.get( Location.class, CNHKG.code().get() );
        STOCKHOLM = uow.get( Location.class, SESTO.code().get() );
        SHANGHAI = uow.get( Location.class, CNSHA.code().get() );
        TOKYO = uow.get( Location.class, JNTKO.code().get() );
        NEWYORK = uow.get( Location.class, USNYC.code().get() );
        DALLAS = uow.get( Location.class, USDAL.code().get() );
        HANGZHOU = uow.get( Location.class, CNHGH.code().get() );
        HELSINKI = uow.get( Location.class, FIHEL.code().get() );
        HAMBURG = uow.get( Location.class, DEHAM.code().get() );
        V100S = uow.get( Voyage.class, "V100S" );
        V200T = uow.get( Voyage.class, "V200T" );
        V300A = uow.get( Voyage.class, "V300A" );
        V400S = uow.get( Voyage.class, "V400S" );
        V500S = uow.get( Voyage.class, "V500S" );

        Cargos CARGOS = uow.get( Cargos.class, CargosEntity.CARGOS_ID );
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, day( 17 ) ).createCargo( "ABC" );
        cargo = uow.get( Cargo.class, trackingId.id().get() );

        itinerary = itinerary(
            leg( V100S, HONGKONG, NEWYORK, day( 1 ), day( 8 ) ),
            leg( V200T, NEWYORK, DALLAS, day( 9 ), day( 12 ) ),
            leg( V300A, DALLAS, STOCKHOLM, day( 13 ), day( 16 ) )
        );


    }

    // DERIVE WITH ROUTE SPECIFICATION ==============================================================================

    @Test( expected = RouteException.class )
    public void deviation_2a_InvalidRouteSpecification_sameLocations() throws Exception
    {
        RouteSpecification routeSpec = routeSpecification( HONGKONG, HONGKONG, day( 20 ) );
        new BuildDeliverySnapshot( routeSpec ).get();
    }

    @Test( expected = RouteException.class )
    public void deviation_2b_InvalidRouteSpecification_tooEarlyDeadline() throws Exception
    {
        RouteSpecification routeSpec = routeSpecification( HONGKONG, STOCKHOLM, TODAY );
        new BuildDeliverySnapshot( routeSpec ).get();
    }

    @Test
    public void deviation_2c_ItineraryIsUnknown_buildFromRouteSpecification() throws Exception
    {
        RouteSpecification routeSpec = routeSpecification( HONGKONG, STOCKHOLM, day( 20 ) );
        Delivery delivery = new BuildDeliverySnapshot( routeSpec ).get();

        assertThat( delivery.timestamp().get().after( TODAY ), is( equalTo( true ) ) ); // TODAY is set first
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.NOT_ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.NOT_RECEIVED ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().handlingEventType().get(), is( equalTo( HandlingEventType.RECEIVE ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().location().get(), is( equalTo( HONGKONG ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().voyage().get(), is( equalTo( null ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( null ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( null ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( null ) ) );
        assertThat( delivery.eta().get(), is( equalTo( null ) ) );
        assertThat( delivery.isMisdirected().get(), is( equalTo( false ) ) );
        assertThat( delivery.isUnloadedAtDestination().get(), is( equalTo( false ) ) );
    }


    // DERIVE WITH NON-ROUTED CARGO ==============================================================================

    @Test
    public void deviation_2c_ItineraryIsUnknown_buildFromNonRoutedCargo() throws Exception
    {
        deviation_2c_ItineraryIsUnknown_buildFromRouteSpecification();

        UnitOfWork uow = module.currentUnitOfWork();
        RouteSpecification routeSpec = routeSpecification( HONGKONG, STOCKHOLM, day( 20 ) );
        Cargos CARGOS = uow.get( Cargos.class, CargosEntity.CARGOS_ID );
        Delivery delivery = new BuildDeliverySnapshot( routeSpec ).get();
        Cargo cargo = CARGOS.createCargo( routeSpec, delivery, "ABCD" );

        // Same as previous test (just build from cargo instead)
        assertThat( delivery.timestamp().get().after( TODAY ), is( equalTo( true ) ) ); // TODAY is set first
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.NOT_ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.NOT_RECEIVED ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().handlingEventType().get(), is( equalTo( HandlingEventType.RECEIVE ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().location().get(), is( equalTo( HONGKONG ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().voyage().get(), is( equalTo( null ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( null ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( null ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( null ) ) );
        assertThat( delivery.eta().get(), is( equalTo( null ) ) );
        assertThat( delivery.isMisdirected().get(), is( equalTo( false ) ) );
        assertThat( delivery.isUnloadedAtDestination().get(), is( equalTo( false ) ) );
    }


    // DERIVE WITH ROUTE SPECIFICATION + ITINERARY (Routed cargo) ==============================================

    @Test
    public void deviation_2d_UnsatisfyingItinerary_wrongOrigin() throws Exception
    {
        deviation_2c_ItineraryIsUnknown_buildFromNonRoutedCargo();

        Itinerary itinerary = itinerary(
            leg( V100S, HONGKONG, NEWYORK, day( 1 ), day( 8 ) ),
            leg( V200T, NEWYORK, DALLAS, day( 9 ), day( 12 ) ),
            leg( V300A, DALLAS, STOCKHOLM, day( 13 ), day( 16 ) )
        );

        // Hangzhou not in itinerary first leg
        RouteSpecification routeSpec = routeSpecification( HANGZHOU, STOCKHOLM, day( 20 ) );
        cargo.itinerary().set( itinerary );
        cargo.routeSpecification().set( routeSpec );
        Delivery delivery = new BuildDeliverySnapshot( cargo ).get();

        // Route specification not satisfied by itinerary
        assertThat( itinerary.firstLeg().loadLocation().get(), is( not( equalTo( routeSpec.origin().get() ) ) ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.MISROUTED ) ) );
    }

    @Test
    public void deviation_2d_UnsatisfyingItinerary_wrongDestination() throws Exception
    {
        deviation_2d_UnsatisfyingItinerary_wrongOrigin();

        // Helsinki not in itinerary last leg
        RouteSpecification routeSpec = routeSpecification( HONGKONG, HELSINKI, day( 20 ) );
        cargo.routeSpecification().set( routeSpec );
        Delivery delivery = new BuildDeliverySnapshot( cargo ).get();

        // Route specification not satisfied by itinerary
        assertThat( itinerary.lastLeg().unloadLocation().get(), is( not( equalTo( routeSpec.destination().get() ) ) ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.MISROUTED ) ) );
    }

    @Test
    public void deviation_2d_UnsatisfyingItinerary_missedDeadline() throws Exception
    {
        deviation_2d_UnsatisfyingItinerary_wrongDestination();

        // Arrival on day 12 according to itinerary is not meeting deadline
        RouteSpecification routeSpec = routeSpecification( HONGKONG, STOCKHOLM, day( 14 ) );
        cargo.routeSpecification().set( routeSpec );
        Delivery delivery = new BuildDeliverySnapshot( cargo ).get();

        // Route specification not satisfied by itinerary
        assertFalse( routeSpec.arrivalDeadline().get().after( itinerary.finalArrivalDate() ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.MISROUTED ) ) );
    }

    @Test
    public void deviation_3a_CargoHasNoHandlingHistory() throws Exception
    {
        deviation_2d_UnsatisfyingItinerary_missedDeadline();

        Date arrival = day( 16 );
        Date deadline = day( 20 );
        // Itinerary will satisfy route specification
        RouteSpecification routeSpec = routeSpecification( HONGKONG, STOCKHOLM, deadline );
        cargo.routeSpecification().set( routeSpec );
        Delivery delivery = new BuildDeliverySnapshot( cargo ).get();

        // Route specification satisfied by itinerary
        assertThat( itinerary.firstLeg().loadLocation().get(), is( equalTo( routeSpec.origin().get() ) ) );
        assertThat( itinerary.lastLeg().unloadLocation().get(), is( equalTo( routeSpec.destination().get() ) ) );
        assertTrue( routeSpec.arrivalDeadline().get().after( itinerary.finalArrivalDate() ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );

        assertThat( delivery.timestamp().get().after( TODAY ), is( equalTo( true ) ) ); // TODAY is set first
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.NOT_RECEIVED ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().handlingEventType().get(), is( equalTo( HandlingEventType.RECEIVE ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().location().get(), is( equalTo( HONGKONG ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().voyage().get(), is( equalTo( null ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( null ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( null ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( null ) ) );
        assertThat( delivery.eta().get(), is( equalTo( arrival ) ) );
        assertThat( delivery.isMisdirected().get(), is( equalTo( false ) ) );
        assertThat( delivery.isUnloadedAtDestination().get(), is( equalTo( false ) ) );
    }


    // DERIVE WITH ROUTE SPECIFICATION + ITINERARY + LAST HANDLING EVENT ============================================

    @Test
    public void deviation_4a_RECEIVE_1a_UnexpectedPort() throws Exception
    {
        deviation_3a_CargoHasNoHandlingHistory();

        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );
        // Unexpected receipt in Shanghai
        HandlingEvent handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 1 ), day( 1 ), trackingId, HandlingEventType.RECEIVE, SHANGHAI, null );
        Delivery delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

        // We don't know what's next for a misdirected cargo
        assertThat( delivery.isMisdirected().get(), is( equalTo( true ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get(), is( equalTo( null ) ) );
        assertThat( delivery.eta().get(), is( equalTo( null ) ) );

        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.IN_PORT ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( handlingEvent ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( SHANGHAI ) ) );

        // Cargo is still routed - but it should be re-routed according to new (unexpected) location.
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( null ) ) );
        assertThat( delivery.isUnloadedAtDestination().get(), is( equalTo( false ) ) );
    }

    @Test
    public void deviation_4a_RECEIVE_1b_ExpectedPort() throws Exception
    {
        deviation_4a_RECEIVE_1a_UnexpectedPort();

        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );
        // Expected receipt in Hong Kong
        HandlingEvent handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 1 ), day( 1 ), trackingId, HandlingEventType.RECEIVE, HONGKONG, null );
        Delivery delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

        assertThat( delivery.isMisdirected().get(), is( equalTo( false ) ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.IN_PORT ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( handlingEvent ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( HONGKONG ) ) );

        // We expect the cargo to be loaded on voyage V100S in Hong Kong
        assertThat( delivery.nextExpectedHandlingEvent().get().handlingEventType().get(), is( equalTo( LOAD ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().location().get(), is( equalTo( HONGKONG ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().voyage().get(), is( equalTo( V100S ) ) );
    }


    @Test
    public void deviation_4b_LOAD_2a_UnexpectedPort() throws Exception
    {
        deviation_4a_RECEIVE_1b_ExpectedPort();

        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );
        // Unexpected load in Tokyo
        HandlingEvent handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 1 ), day( 1 ), trackingId, LOAD, TOKYO, V100S );
        Delivery delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

        assertThat( delivery.isMisdirected().get(), is( equalTo( true ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get(), is( equalTo( null ) ) );
        assertThat( delivery.eta().get(), is( equalTo( null ) ) );

        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.ONBOARD_CARRIER ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( handlingEvent ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( TOKYO ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( V100S ) ) );
    }

    @Test
    public void deviation_4b_LOAD_2b_ExpectedPort() throws Exception
    {
        deviation_4b_LOAD_2a_UnexpectedPort();

        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );
        // Expected load in Hong Kong
        HandlingEvent handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 1 ), day( 1 ), trackingId, LOAD, HONGKONG, V100S );
        Delivery delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

        assertThat( delivery.isMisdirected().get(), is( equalTo( false ) ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.ONBOARD_CARRIER ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( handlingEvent ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( HONGKONG ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( V100S ) ) );

        // We expect the cargo to be unloaded from voyage V100S in New York
        assertThat( delivery.nextExpectedHandlingEvent().get().handlingEventType().get(), is( equalTo( UNLOAD ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().location().get(), is( equalTo( NEWYORK ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().voyage().get(), is( equalTo( V100S ) ) );
    }

    @Test
    public void deviation_4b_LOAD_2c_UnexpectedVoyageNotFromItinerary() throws Exception
    {
        deviation_4b_LOAD_2b_ExpectedPort();

        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );
        // Load onto unexpected voyage
        HandlingEvent handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 1 ), day( 1 ), trackingId, LOAD, HONGKONG, V400S );
        Delivery delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

        assertThat( delivery.isMisdirected().get(), is( equalTo( true ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get(), is( equalTo( null ) ) );
        assertThat( delivery.eta().get(), is( equalTo( null ) ) );

        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.ONBOARD_CARRIER ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( handlingEvent ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( HONGKONG ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( V400S ) ) );
    }

    @Test
    public void deviation_4b_LOAD_2c_ExpectedButLaterVoyageInItinerary() throws Exception
    {
        deviation_4b_LOAD_2c_UnexpectedVoyageNotFromItinerary();

        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );

        // The system doesn't currently check if handling events happen in the right order, so
        // a cargo can now suddenly load in New York, even though it hasn't got there yet.
        HandlingEvent handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 5 ), day( 5 ), trackingId, LOAD, NEWYORK, V200T );
        Delivery delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

        // Should have been true, but we accept it for now...
        assertThat( delivery.isMisdirected().get(), is( equalTo( false ) ) );

        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.ONBOARD_CARRIER ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( handlingEvent ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( NEWYORK ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( V200T ) ) );

        // We expect the cargo to be unloaded from voyage V200T in Dallas
        assertThat( delivery.nextExpectedHandlingEvent().get().handlingEventType().get(), is( equalTo( UNLOAD ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().location().get(), is( equalTo( DALLAS ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().voyage().get(), is( equalTo( V200T ) ) );
    }


    @Test
    public void deviation_4c_UNLOAD_1a_UnexpectedPort() throws Exception
    {
        deviation_4b_LOAD_2c_ExpectedButLaterVoyageInItinerary();

        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );

        // Unexpected unload in Tokyo
        HandlingEvent handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 5 ), day( 5 ), trackingId, UNLOAD, TOKYO, V100S );
        Delivery delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();
        cargo.delivery().set( delivery );

        assertThat( delivery.isMisdirected().get(), is( equalTo( true ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get(), is( equalTo( null ) ) );
        assertThat( delivery.eta().get(), is( equalTo( null ) ) );

        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.IN_PORT ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( handlingEvent ) ) );
        assertThat( delivery.lastHandlingEvent().get().handlingEventType().get(), is( equalTo( UNLOAD ) ) );
        assertThat( delivery.lastHandlingEvent().get().voyage().get(), is( equalTo( V100S ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( TOKYO ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( null ) ) );
        assertThat( delivery.isUnloadedAtDestination().get(), is( equalTo( false ) ) );

        // Cargo needs to be rerouted

        // Customer specifies a new route
        RouteSpecification routeSpec = routeSpecification( TOKYO, STOCKHOLM, day( 20 ) );
        cargo.routeSpecification().set( routeSpec );
        delivery = new BuildDeliverySnapshot( cargo ).get();
        cargo.delivery().set( delivery );

        // Old itinerary will not satisfy new route specification
        assertThat( itinerary.firstLeg().loadLocation().get(), is( not( equalTo( routeSpec.origin().get() ) ) ) );
        assertThat( itinerary.lastLeg().unloadLocation().get(), is( equalTo( routeSpec.destination().get() ) ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.MISROUTED ) ) );

        // Old planned arrival time is still satisfying new deadline
        assertTrue( routeSpec.arrivalDeadline().get().after( itinerary.finalArrivalDate() ) );

        // We don't know what's next before a new itinerary has been chosen
        assertThat( delivery.nextExpectedHandlingEvent().get(), is( equalTo( null ) ) );
        assertThat( delivery.eta().get(), is( equalTo( null ) ) );

        // Cargo is still misdirected (in unexpected location) according to old itinerary
        assertThat( delivery.isMisdirected().get(), is( equalTo( true ) ) );

        // Last known data
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.IN_PORT ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( handlingEvent ) ) );
        assertThat( delivery.lastHandlingEvent().get().handlingEventType().get(), is( equalTo( UNLOAD ) ) );
        assertThat( delivery.lastHandlingEvent().get().voyage().get(), is( equalTo( V100S ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( TOKYO ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( null ) ) );
        assertThat( delivery.isUnloadedAtDestination().get(), is( equalTo( false ) ) );

        // New itinerary that satisfy the new route specification. New origin departure from Tokyo.
        Date arrival= day( 19 );
        itinerary = itinerary(
              leg( V400S, TOKYO, HAMBURG, day( 9 ), day( 16 ) ),
              leg( V500S, HAMBURG, STOCKHOLM, day( 17 ), arrival  )
        );

        // Customer reroutes cargo. This is a possible step in the cargo booking process.
        new BookNewCargo( cargo, itinerary ).assignCargoToRoute();
        delivery = cargo.delivery().get();

        // Cargo is on track again
        assertThat( delivery.isMisdirected().get(), is( equalTo( false ) ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.IN_PORT ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( handlingEvent ) ) );
        assertThat( delivery.lastHandlingEvent().get().handlingEventType().get(), is( equalTo( UNLOAD ) ) );
        assertThat( delivery.lastHandlingEvent().get().voyage().get(), is( equalTo( V100S ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( TOKYO ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( null ) ) );
        assertThat( delivery.eta().get(), is( equalTo( arrival ) ) );
        assertThat( delivery.isUnloadedAtDestination().get(), is( equalTo( false ) ) );

        // When a cargo is rerouted the (often misdirected) last handling event is flagged as disregarded
        // since it doesn't have to be part of the new itinerary (this isn't in the Citerus version).

        // We now expect the cargo to be loaded onto voyage V400S in Tokyo heading to Hamburg
        assertThat( delivery.isMisdirected().get(), is( equalTo( false ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().handlingEventType().get(), is( equalTo( LOAD ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().location().get(), is( equalTo( TOKYO ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().voyage().get(), is( equalTo( V400S ) ) );

        // Cargo is not misdirected anymore according to new itinerary. Cargo location is now expected to be in Tokyo.
    }

    @Test
    public void deviation_4c_UNLOAD_1b_ExpectedMidpointLocation() throws Exception
    {
        deviation_4c_UNLOAD_1a_UnexpectedPort();

        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );

        // Unload at midpoint location of itinerary
        HandlingEvent handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 8 ), day( 8 ), trackingId, UNLOAD, HAMBURG, V400S );
        Delivery delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

        assertThat( delivery.isMisdirected().get(), is( equalTo( false ) ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.IN_PORT ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( handlingEvent ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( HAMBURG ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( null ) ) );
        assertThat( delivery.isUnloadedAtDestination().get(), is( equalTo( false ) ) );

        // We expect the cargo to be loaded onto voyage V200T in New York heading for Dallas
        assertThat( delivery.nextExpectedHandlingEvent().get().handlingEventType().get(), is( equalTo( LOAD ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().location().get(), is( equalTo( HAMBURG ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().voyage().get(), is( equalTo( V500S ) ) );
    }

    @Test
    public void deviation_4c_UNLOAD_1c_Destination() throws Exception
    {
        deviation_4c_UNLOAD_1b_ExpectedMidpointLocation();

        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );

        // Unload at destination
        HandlingEvent handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 16 ), day( 16 ), trackingId, UNLOAD, STOCKHOLM, V500S );
        Delivery delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

        assertThat( delivery.isMisdirected().get(), is( equalTo( false ) ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.IN_PORT ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( handlingEvent ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( STOCKHOLM ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( null ) ) );

        // Cargo has arrived at destination location
        assertThat( delivery.isUnloadedAtDestination().get(), is( equalTo( true ) ) );

        // We expect the cargo to be claimed by customer
        assertThat( delivery.nextExpectedHandlingEvent().get().handlingEventType().get(), is( equalTo( CLAIM ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().location().get(), is( equalTo( STOCKHOLM ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().voyage().get(), is( equalTo( null ) ) );
    }


    @Test
    public void deviation_4d_CUSTOMS_1a_CargoIsInDestinationPort() throws Exception
    {
        deviation_4c_UNLOAD_1c_Destination();

        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );

        // Cargo was handled by the customs authorities
        HandlingEvent handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 16 ), day( 16 ), trackingId, CUSTOMS, STOCKHOLM, null );
        Delivery delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

        assertThat( delivery.isMisdirected().get(), is( equalTo( false ) ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.IN_PORT ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( handlingEvent ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( STOCKHOLM ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( null ) ) );

        // Cargo might be at destination, but the last handling event wasn't unloading
        assertThat( delivery.isUnloadedAtDestination().get(), is( equalTo( true ) ) );

        // Shouldn't we expect the cargo to be claimed by the customer now ?
        assertThat( delivery.nextExpectedHandlingEvent().get(), is( equalTo( null ) ) );
    }


    @Test
    public void deviation_4e_CLAIM_1a_CargoIsNotInDestinationPort() throws Exception
    {
        deviation_4d_CUSTOMS_1a_CargoIsInDestinationPort();

        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );

        // Cargo was claimed but not at destination location
        HandlingEvent handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 1 ), day( 16 ), trackingId, CLAIM, HELSINKI, null );
        Delivery delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

        assertThat( delivery.isMisdirected().get(), is( equalTo( true ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get(), is( equalTo( null ) ) );
        assertThat( delivery.eta().get(), is( equalTo( null ) ) );

        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.CLAIMED ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( handlingEvent ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( HELSINKI ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( null ) ) );

        // Cargo is claimed but has not arrived yet in destination port
        assertThat( delivery.isUnloadedAtDestination().get(), is( equalTo( false ) ) );
    }

    @Test
    public void deviation_4e_CLAIM_1b_CargoIsInDestinationPort() throws Exception
    {
        deviation_4e_CLAIM_1a_CargoIsNotInDestinationPort();

        UnitOfWork uow = module.currentUnitOfWork();
        HandlingEventsEntity HANDLING_EVENTS = uow.get( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );

        // Cargo was claimed by customer at destination location
        HandlingEvent handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 16 ), day( 16 ), trackingId, CLAIM, STOCKHOLM, null );
        Delivery delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

        assertThat( delivery.isMisdirected().get(), is( equalTo( false ) ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.CLAIMED ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( handlingEvent ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( STOCKHOLM ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( null ) ) );

        // Cargo is claimed in destination port
        assertThat( delivery.isUnloadedAtDestination().get(), is( equalTo( true ) ) );

        // No more expected handling events
        assertThat( delivery.nextExpectedHandlingEvent().get(), is( equalTo( null ) ) );
    }
}