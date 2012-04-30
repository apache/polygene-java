package com.marcgrue.dcisample_b.context.test.handling.inspection.event;

import com.marcgrue.dcisample_b.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.event.InspectArrivedCargo;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.CargoArrivedException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.*;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.ONBOARD_CARRIER;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.CLAIM;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.UNLOAD;
import static org.junit.Assert.fail;

/**
 * {@link InspectArrivedCargo} tests
 */
public class InspectArrivedCargoTest extends TestApplication
{
    @BeforeClass
    public static void setup() throws Exception
    {
        TestApplication.setup();

        // Create new cargo
        routeSpec = routeSpecFactory.build( HONGKONG, STOCKHOLM, new Date(), deadline = DAY24 );
        delivery = delivery( TODAY, IN_PORT, ROUTED, leg5 );
        cargo = CARGOS.createCargo( routeSpec, delivery, "Arrived_CARGO" );
        trackingId = cargo.trackingId().get();
    }

    @Test
    public void deviation_2a_NotRouted_MissingItinerary_UnloadedInFinalDestination() throws Exception
    {
        // Cargo not routed
        cargo.itinerary().set( null );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, NOT_ROUTED, leg5 ) );

        // Unload in final destination (with no itinerary!)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY23, DAY23, trackingId, UNLOAD, STOCKHOLM, V203 );
        try
        {
            new InspectArrivedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch (CargoArrivedException e)
        {
            assertMessage( e, "Cargo 'Arrived_CARGO' has arrived in destination Stockholm (SESTO)" );

            // An unexpected unload shouldn't be considered an itinerary progress - legIndex stays unchanged
            assertDelivery( UNLOAD, STOCKHOLM, DAY23, V203,
                            IN_PORT, arrived,
                            NOT_ROUTED, directed, unknownETA, unknownLeg,
                            CLAIM, STOCKHOLM, DAY23, noVoyage  );
        }
    }

    @Test
    public void deviation_2b_Misrouted_WrongItineraryWithoutCurrentUnloadLocation_UnloadedInFinalDestination() throws Exception
    {
        // Misroute cargo - assign unsatisfying itinerary not going to Stockholm
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, MISROUTED, leg5 ) );

        // Unload in final destination (with wrong itinerary)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY23, DAY23, trackingId, UNLOAD, STOCKHOLM, V203 );
        try
        {
            new InspectArrivedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch (CargoArrivedException e)
        {
            assertMessage( e, "Cargo 'Arrived_CARGO' has arrived in destination Stockholm (SESTO)" );
            assertDelivery( UNLOAD, STOCKHOLM, DAY23, V203,
                            IN_PORT, arrived,
                            MISROUTED, directed, unknownETA, unknownLeg,
                            CLAIM, STOCKHOLM, DAY23, noVoyage );
        }
    }

    @Test
    public void success_UnloadInDestination() throws Exception
    {
        // Assign satisfying route going to Stockholm
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, ROUTED, leg5 ) );

        // Unload in final destination (with satisfying itinerary)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY23, DAY23, trackingId, UNLOAD, STOCKHOLM, V203 );
        try
        {
            new InspectArrivedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch (CargoArrivedException e)
        {
            assertMessage( e, "Cargo 'Arrived_CARGO' has arrived in destination Stockholm (SESTO)" );
            assertDelivery( UNLOAD, STOCKHOLM, DAY23, V203,
                            IN_PORT, arrived,
                            ROUTED, directed, itinerary.eta(),leg5,
                            CLAIM, STOCKHOLM, DAY23, noVoyage );
        }
    }
}
