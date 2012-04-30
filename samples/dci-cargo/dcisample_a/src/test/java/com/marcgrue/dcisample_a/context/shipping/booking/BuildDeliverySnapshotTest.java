package com.marcgrue.dcisample_a.context.shipping.booking;

import com.marcgrue.dcisample_a.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_a.data.shipping.delivery.RoutingStatus;
import com.marcgrue.dcisample_a.data.shipping.delivery.TransportStatus;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEventType;
import org.junit.Test;

import static com.marcgrue.dcisample_a.data.shipping.handling.HandlingEventType.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Tests of the Build Delivery Snapshot subfunction use case.
 *
 * All deviations of the use case are tested one by one. The business rules are therefore enforcing the
 * structure for the test suite and not arbitrary ideas of the programmer what to test. If the business
 * analyst haven't foreseen a deviation in the use case, it's his responsibility that it's not tested.
 *
 * Test method names describe the test purpose. The prefix refers to the step in the use case.
 */
public class BuildDeliverySnapshotTest
      extends TestApplication
{
    // DERIVE WITH ROUTE SPECIFICATION ==============================================================================

    @Test( expected = Exception.class )
    public void deviation_2a_InvalidRouteSpecification_sameLocations() throws Exception
    {
        routeSpec = routeSpecification( HONGKONG, HONGKONG, deadline = day( 20 ) );
        new BuildDeliverySnapshot( routeSpec ).get();
    }

    @Test( expected = Exception.class )
    public void deviation_2b_InvalidRouteSpecification_tooEarlyDeadline() throws Exception
    {
        routeSpec = routeSpecification( HONGKONG, STOCKHOLM, TODAY );
        new BuildDeliverySnapshot( routeSpec ).get();
    }

    @Test
    public void deviation_2c_ItineraryIsUnknown_buildFromRouteSpecification() throws Exception
    {
        routeSpec = routeSpecification( HONGKONG, STOCKHOLM, day( 20 ) );
        delivery = new BuildDeliverySnapshot( routeSpec ).get();

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
        cargo = CARGOS.createCargo( routeSpec, delivery, "ABCD" );
        trackingId = cargo.trackingId().get();
        delivery = new BuildDeliverySnapshot( cargo ).get();

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
        itinerary = itinerary(
              leg( V100S, HONGKONG, NEWYORK, day( 1 ), day( 8 ) ),
              leg( V200T, NEWYORK, DALLAS, day( 9 ), day( 12 ) ),
              leg( V300A, DALLAS, STOCKHOLM, day( 13 ), arrival = day( 16 ) )
        );

        // Hangzhou not in itinerary first leg
        routeSpec = routeSpecification( HANGZHOU, STOCKHOLM, day( 20 ) );
        cargo.itinerary().set( itinerary );
        cargo.routeSpecification().set( routeSpec );
        delivery = new BuildDeliverySnapshot( cargo ).get();

        // Route specification not satisfied by itinerary
        assertThat( itinerary.firstLeg().loadLocation().get(), is( not( equalTo( routeSpec.origin().get() ) ) ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.MISROUTED ) ) );
    }

    @Test
    public void deviation_2d_UnsatisfyingItinerary_wrongDestination() throws Exception
    {
        // Helsinki not in itinerary last leg
        routeSpec = routeSpecification( HONGKONG, HELSINKI, day( 20 ) );
        cargo.routeSpecification().set( routeSpec );
        delivery = new BuildDeliverySnapshot( cargo ).get();

        // Route specification not satisfied by itinerary
        assertThat( itinerary.lastLeg().unloadLocation().get(), is( not( equalTo( routeSpec.destination().get() ) ) ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.MISROUTED ) ) );
    }

    @Test
    public void deviation_2d_UnsatisfyingItinerary_missedDeadline() throws Exception
    {
        // Arrival on day 12 according to itinerary is not meeting deadline
        routeSpec = routeSpecification( HONGKONG, STOCKHOLM, deadline = day( 14 ) );
        cargo.routeSpecification().set( routeSpec );
        delivery = new BuildDeliverySnapshot( cargo ).get();

        // Route specification not satisfied by itinerary
        assertFalse( routeSpec.arrivalDeadline().get().after( itinerary.finalArrivalDate() ) );
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.MISROUTED ) ) );
    }

    @Test
    public void deviation_3a_CargoHasNoHandlingHistory() throws Exception
    {
        // Itinerary will satisfy route specification
        routeSpec = routeSpecification( HONGKONG, STOCKHOLM, deadline = day( 20 ) );
        cargo.routeSpecification().set( routeSpec );
        delivery = new BuildDeliverySnapshot( cargo ).get();

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
        // Unexpected receipt in Shanghai
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 1 ), day( 1 ), trackingId, HandlingEventType.RECEIVE, SHANGHAI, null );
        delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

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
        // Expected receipt in Hong Kong
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 1 ), day( 1 ), trackingId, HandlingEventType.RECEIVE, HONGKONG, null );
        delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

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
        // Unexpected load in Tokyo
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 1 ), day( 1 ), trackingId, LOAD, TOKYO, V100S );
        delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

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
        // Expected load in Hong Kong
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 1 ), day( 1 ), trackingId, LOAD, HONGKONG, V100S );
        delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

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
        // Load onto unexpected voyage
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 1 ), day( 1 ), trackingId, LOAD, HONGKONG, V400S );
        delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

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
        /*
       * The system doesn't currently check if handling events happen in the right order, so
       * a cargo can now suddenly load in New York, even though it hasn't got there yet.
       * */
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 5 ), day( 5 ), trackingId, LOAD, NEWYORK, V200T );
        delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

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
        // Unexpected unload in Tokyo
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 5 ), day( 5 ), trackingId, UNLOAD, TOKYO, V100S );
        delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();
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
        routeSpec = routeSpecification( TOKYO, STOCKHOLM, deadline = day( 20 ) );
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
        itinerary = itinerary(
              leg( V400S, TOKYO, HAMBURG, day( 9 ), day( 16 ) ),
              leg( V500S, HAMBURG, STOCKHOLM, day( 17 ), arrival = day( 19 ) )
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

        /*
       * When a cargo is rerouted the (often misdirected) last handling event is flagged as disregarded
       * since it doesn't have to be part of the new itinerary (this isn't in the Citerus version).
       * */

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
        // Unload at midpoint location of itinerary
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 8 ), day( 8 ), trackingId, UNLOAD, HAMBURG, V400S );
        delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

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
        // Unload at destination
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 16 ), day( 16 ), trackingId, UNLOAD, STOCKHOLM, V500S );
        delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

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
        // Cargo was handled by the customs authorities
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 16 ), day( 16 ), trackingId, CUSTOMS, STOCKHOLM, null );
        delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

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
        // Cargo was claimed but not at destination location
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 1 ), day( 16 ), trackingId, CLAIM, HELSINKI, null );
        delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

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
        // Cargo was claimed by customer at destination location
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 16 ), day( 16 ), trackingId, CLAIM, STOCKHOLM, null );
        delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();

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