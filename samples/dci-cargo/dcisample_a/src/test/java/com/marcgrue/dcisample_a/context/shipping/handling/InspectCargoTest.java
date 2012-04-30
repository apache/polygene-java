package com.marcgrue.dcisample_a.context.shipping.handling;

import com.marcgrue.dcisample_a.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_a.context.shipping.booking.BookNewCargo;
import com.marcgrue.dcisample_a.context.shipping.booking.BuildDeliverySnapshot;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEventType;
import com.marcgrue.dcisample_a.data.shipping.cargo.Cargo;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEventType;
import org.junit.Test;

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
    @Test
    public void deviation_3a_CargoIsMisdirected() throws Exception
    {
        // Create cargo
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, day( 17 ) ).createCargo( "ABC" );
        cargo = uow.get( Cargo.class, trackingId.id().get() );
        itinerary = itinerary(
              leg( V100S, HONGKONG, NEWYORK, day( 1 ), day( 8 ) ),
              leg( V200T, NEWYORK, DALLAS, day( 9 ), day( 12 ) ),
              leg( V300A, DALLAS, STOCKHOLM, day( 13 ), arrival = day( 16 ) )
        );

        // Route cargo
        new BookNewCargo( cargo, itinerary ).assignCargoToRoute();

        // Create misdirected handling event for cargo (receipt in Shanghai is unexpected)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 0 ), day( 0 ), trackingId, HandlingEventType.RECEIVE, SHANGHAI, null );
        delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();
        cargo.delivery().set( delivery );
        assertThat( cargo.delivery().get().isMisdirected().get(), is( equalTo( true ) ) );

        logger.info( "  Handling cargo 'ABC' (misdirected):" );
        new InspectCargo( handlingEvent ).inspect();

        // Assert that notification of misdirection has been sent (see console output).
    }

    @Test
    public void deviation_3b_CargoHasArrived() throws Exception
    {

        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 15 ), day( 15 ), trackingId, HandlingEventType.UNLOAD, STOCKHOLM, V300A );
        delivery = new BuildDeliverySnapshot( cargo, handlingEvent ).get();
        cargo.delivery().set( delivery );
        assertThat( cargo.delivery().get().isUnloadedAtDestination().get(), is( equalTo( true ) ) );

        logger.info( "  Handling cargo 'ABC' (arrived):" );
        new InspectCargo( handlingEvent ).inspect();

        // Assert that notification of  arrival has been sent (see console output).
    }

    @Test
    public void step_3_CargoIsCorrectlyInTransit() throws Exception
    {
        logger.info( "  Handling cargo 'ABC' (unloaded in Dallas):" );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( day( 12 ), day( 12 ), trackingId, HandlingEventType.UNLOAD, DALLAS, V200T );
        cargo.delivery().set( new BuildDeliverySnapshot( cargo, handlingEvent ).get() );
        assertThat( cargo.delivery().get().isMisdirected().get(), is( equalTo( false ) ) );
        new InspectCargo( handlingEvent ).inspect();

        logger.info( "  Cargo was correctly directed." );
    }
}