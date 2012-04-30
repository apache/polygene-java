package com.marcgrue.dcisample_b.context.test.handling.inspection.event;

import com.marcgrue.dcisample_b.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.event.InspectCargoInCustoms;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.InspectionFailedException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.*;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.ONBOARD_CARRIER;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.CUSTOMS;
import static org.junit.Assert.fail;

/**
 * {@link InspectCargoInCustoms} tests
 */
public class InspectCargoInCustomsTest extends TestApplication
{
    @BeforeClass
    public static void setup() throws Exception
    {
        TestApplication.setup();

        // Create new cargo
        routeSpec = routeSpecFactory.build( HONGKONG, STOCKHOLM, new Date(), deadline = DAY24 );
        delivery = delivery( TODAY, IN_PORT, ROUTED, leg1 );
        cargo = CARGOS.createCargo( routeSpec, delivery, "CARGO_in_customs" );
        trackingId = cargo.trackingId().get();
    }

    @Test
    public void precondition_CustomsHandlingNotOnBoardCarrier() throws Exception
    {
        cargo.itinerary().set( itinerary );

        // No customs handling on board a carrier...
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, CUSTOMS, STOCKHOLM, noVoyage );
        cargo.delivery().set( delivery( handlingEvent, ONBOARD_CARRIER, notArrived,
                                        ROUTED, directed, unknownETA, unknownLeg,
                                        unknownNextHandlingEvent ) );
        try
        {
            new InspectCargoInCustoms( cargo, handlingEvent ).inspect();
            fail();
        }
        catch (InspectionFailedException e)
        {
            assertMessage( e, "INTERNAL ERROR: Cannot handle cargo in customs on board a carrier." );
        }
    }

    @Test
    public void deviation_2a_NotRouted_CustomsLocation_FinalDestination() throws Exception
    {
        // Cargo not routed
        cargo.itinerary().set( null );
        cargo.delivery().set( delivery( TODAY, IN_PORT, NOT_ROUTED, leg5 ) );

        // Handle in customs (without itinerary!)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, CUSTOMS, STOCKHOLM, noVoyage );
        new InspectCargoInCustoms( cargo, handlingEvent ).inspect();

        assertDelivery( CUSTOMS, STOCKHOLM, DAY1, noVoyage,
                        IN_PORT, notArrived,
                        NOT_ROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }

    @Test
    public void deviation_2b_Misrouted_CustomsLocation_DestinationOfWrongItinerary() throws Exception
    {
        // Misroute cargo - assign unsatisfying itinerary not going to Stockholm
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, MISROUTED, leg3 ) );

        // Handle in customs (with wrong itinerary)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY20, DAY20, trackingId, CUSTOMS, MELBOURNE, noVoyage );
        new InspectCargoInCustoms( cargo, handlingEvent ).inspect();

        assertDelivery( CUSTOMS, MELBOURNE, DAY20, noVoyage,
                        IN_PORT, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }

    @Test
    public void step_2_Routed_CustomsLocation_FinalDestination() throws Exception
    {
        // Assign satisfying route going to Stockholm
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, MISROUTED, leg5 ) );

        // Handle in customs (without itinerary!)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY24, DAY24, trackingId, CUSTOMS, STOCKHOLM, noVoyage );
        new InspectCargoInCustoms( cargo, handlingEvent ).inspect();

        assertDelivery( CUSTOMS, STOCKHOLM, DAY24, noVoyage,
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary.eta(), leg5,
                        unknownNextHandlingEvent );
    }
}
