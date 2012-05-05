package com.marcgrue.dcisample_a.context.shipping.booking;

import com.marcgrue.dcisample_a.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_a.context.support.FoundNoRoutesException;
import com.marcgrue.dcisample_a.data.entity.CargoEntity;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEventType;
import com.marcgrue.dcisample_a.data.shipping.itinerary.Itinerary;
import com.marcgrue.dcisample_a.data.shipping.cargo.Cargo;
import com.marcgrue.dcisample_a.data.shipping.delivery.RoutingStatus;
import com.marcgrue.dcisample_a.data.shipping.delivery.TransportStatus;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test of Book New Cargo use case.
 *
 * This is a test suite where all steps and deviations in the use case are tested.
 * Some data will carry over from one test to another (all tests run within the same UnitOfWork).
 *
 * Test method names describe the test purpose. The prefix refers to the step in the use case.
 */
public class BookNewCargoTest
      extends TestApplication
{

    @Test( expected = Exception.class )
    public void deviation2a_OriginAndDestinationSame() throws Exception
    {
        new BookNewCargo( CARGOS, HONGKONG, HONGKONG, day( 17 ) ).book();
    }

    @Test( expected = Exception.class )
    public void deviation_2b_1_DeadlineInThePastNotAccepted() throws Exception
    {
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, day( -1 ) ).book();
    }

    @Test( expected = Exception.class )
    public void deviation_2b_2_DeadlineTodayIsTooEarly() throws Exception
    {
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, day( 0 ) ).book();
    }

    @Test
    public void deviation_2b_3_DeadlineTomorrowIsOkay() throws Exception
    {
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, day( 1 ) ).book();
    }

    @Test
    public void step_2_CreateNewCargo() throws Exception
    {
        // Create cargo with valid input from customer
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, day( 17 ) ).book();

        // Retrieve created cargo from store
        cargo = uow.get( CargoEntity.class, trackingId.id().get() );

        // Test cargo data
        assertThat( cargo.trackingId().get(), is( equalTo( trackingId ) ) );
        assertThat( cargo.origin().get(), is( equalTo( HONGKONG ) ) );

        // Test route specification
        assertThat( cargo.routeSpecification().get().destination().get(), is( equalTo( STOCKHOLM ) ) );
        // day(17) here is calculated a few milliseconds after initial day(17), so it will be later...
        assertTrue( cargo.routeSpecification().get().arrivalDeadline().get().before( day( 17 ) ) );

        // (Itinerary is not assigned yet)

        // Test derived delivery snapshot
        delivery = cargo.delivery().get();
        assertThat( delivery.timestamp().get().after( TODAY ), is( equalTo( true ) ) ); // TODAY is set first
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.NOT_ROUTED ) ) );
        assertThat( delivery.transportStatus().get(), is( equalTo( TransportStatus.NOT_RECEIVED ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().handlingEventType().get(), is( equalTo( HandlingEventType.RECEIVE ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().location().get(), is( equalTo( HONGKONG ) ) );
        assertThat( delivery.nextExpectedHandlingEvent().get().voyage().get(), is( equalTo( null ) ) );
        assertThat( delivery.lastHandlingEvent().get(), is( equalTo( null ) ) );
        assertThat( delivery.lastKnownLocation().get(), is( equalTo( null ) ) );
        assertThat( delivery.currentVoyage().get(), is( equalTo( null ) ) );
        assertThat( delivery.eta().get(), is( equalTo( null ) ) ); // Is set when itinerary is assigned
        assertThat( delivery.isMisdirected().get(), is( equalTo( false ) ) );
        assertThat( delivery.isUnloadedAtDestination().get(), is( equalTo( false ) ) );
    }

    @Test( expected = FoundNoRoutesException.class )
    public void deviation_3a_NoRoutesCanBeThatFast() throws Exception
    {
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, day( 1 ) ).book();
        cargo = uow.get( Cargo.class, trackingId.id().get() );

        // No routes will be found
        new BookNewCargo( cargo ).routeCandidates();
    }

    @Test
    public void step_3_CalculatePossibleRoutes() throws Exception
    {
        // Create valid cargo
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, deadline = day( 30 ) ).book();
        cargo = uow.get( Cargo.class, trackingId.id().get() );

        // Step 3 - Find possible routes
        routeCandidates = new BookNewCargo( cargo ).routeCandidates();

        // Check possible routes
        for (Itinerary itinerary : routeCandidates)
        {
            assertThat( "First load location equals origin location.",
                        itinerary.firstLeg().loadLocation().get(),
                        is( equalTo( cargo.routeSpecification().get().origin().get() ) ) );
            assertThat( "Last unload location equals destination location.",
                        itinerary.lastLeg().unloadLocation().get(),
                        is( equalTo( cargo.routeSpecification().get().destination().get() ) ) );
            assertThat( "Cargo will be delivered in time.",
                        itinerary.finalArrivalDate().before( cargo.routeSpecification().get().arrivalDeadline().get() ),
                        is( equalTo( true ) ) );
        }
    }

    @Test
    public void step_5_AssignCargoToRoute() throws Exception
    {
        cargo = uow.get( Cargo.class, trackingId.id().get() );

        // Get first route found in last test
        // Would normally be found with an Itinerary id from customer selection
        itinerary = routeCandidates.get( 0 );

        // Use case step 5 - System assigns cargo to route
        new BookNewCargo( cargo, itinerary ).assignCargoToRoute();

        assertThat( "Itinerary has been assigned to cargo.", itinerary, is( equalTo( cargo.itinerary().get() ) ) );

        // BuildDeliverySnapshot will check if itinerary is valid. No need to check it here.

        // Check values set in new delivery snapshot
        delivery = cargo.delivery().get();
        assertThat( delivery.routingStatus().get(), is( equalTo( RoutingStatus.ROUTED ) ) );

        // ETA (= Unload time of last Leg) is before Deadline (set in previous test)
        assertTrue( delivery.eta().get().before( deadline ) );
    }
}
