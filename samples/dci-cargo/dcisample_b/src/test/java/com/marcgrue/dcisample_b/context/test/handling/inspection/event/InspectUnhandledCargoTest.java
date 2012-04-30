package com.marcgrue.dcisample_b.context.test.handling.inspection.event;

import com.marcgrue.dcisample_b.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.event.InspectUnhandledCargo;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.InspectionFailedException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.*;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.*;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.*;
import static org.junit.Assert.fail;

/**
 * {@link InspectUnhandledCargo} tests
 */
public class InspectUnhandledCargoTest extends TestApplication
{
    @BeforeClass
    public static void setup() throws Exception
    {
        TestApplication.setup();

        // Create new cargo
        routeSpec = routeSpecFactory.build( HONGKONG, STOCKHOLM, new Date(), deadline = DAY24 );
        delivery = delivery( TODAY, NOT_RECEIVED, NOT_ROUTED, leg1 );
        cargo = CARGOS.createCargo( routeSpec, delivery, "Claimed_CARGO" );
        trackingId = cargo.trackingId().get();
    }



    @Test
    public void precondition_CannotInspectUnloadInDestinationHere() throws Exception
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
        catch (InspectionFailedException e)
        {
            assertMessage( e, "INTERNAL ERROR: Can only inspect unhandled cargo" );
        }
    }

    @Test
    public void deviation_2a_NotRouted() throws Exception
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
    public void deviation_2b_Misrouted() throws Exception
    {
        // Misroute cargo - assign unsatisfying itinerary not going to Stockholm
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, MISROUTED, leg1 ) );

        new InspectUnhandledCargo( cargo ).inspect();

        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        RECEIVE, HONGKONG, noSpecificDate, noVoyage  );
    }

    @Test
    public void step_2_Routed() throws Exception
    {
        // Assign satisfying route going to Stockholm
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, NOT_RECEIVED, ROUTED, leg1 ) );

        new InspectUnhandledCargo( cargo ).inspect();

        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        ROUTED, directed, itinerary.eta(), leg1,
                        RECEIVE, HONGKONG, noSpecificDate, noVoyage  );
    }
}
