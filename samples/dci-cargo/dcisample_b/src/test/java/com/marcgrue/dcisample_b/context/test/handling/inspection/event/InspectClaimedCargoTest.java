package com.marcgrue.dcisample_b.context.test.handling.inspection.event;

import com.marcgrue.dcisample_b.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.event.InspectClaimedCargo;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.*;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.CLAIMED;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.CLAIM;

/**
 * {@link InspectClaimedCargo} tests
 */
public class InspectClaimedCargoTest extends TestApplication
{
    @BeforeClass
    public static void setup() throws Exception
    {
        TestApplication.setup();

        // Create new cargo
        routeSpec = routeSpecFactory.build( HONGKONG, STOCKHOLM, new Date(), deadline = DAY24 );
        delivery = delivery( TODAY, IN_PORT, ROUTED, leg1 );
        cargo = CARGOS.createCargo( routeSpec, delivery, "Claimed_CARGO" );
        trackingId = cargo.trackingId().get();
    }

    @Test
    public void deviation_2a_NotRouted_ClaimInFinalDestination() throws Exception
    {
        // Cargo not routed
        cargo.itinerary().set( null );
        cargo.delivery().set( delivery( TODAY, IN_PORT, NOT_ROUTED, leg5 ) );

        // Claim in final destination (without itinerary!)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, CLAIM, STOCKHOLM, noVoyage );


        new InspectClaimedCargo( cargo, handlingEvent ).inspect();

        assertDelivery( CLAIM, STOCKHOLM, DAY1, noVoyage,
                        CLAIMED, notArrived,
                        NOT_ROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }

    @Test
    public void deviation_2b_Misrouted_ClaimInDestinationOfWrongItinerary() throws Exception
    {
        // Misroute cargo - assign unsatisfying itinerary not going to Stockholm
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, MISROUTED, leg3 ) );

        // Claim in final destination (with wrong itinerary)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY20, DAY20, trackingId, CLAIM, MELBOURNE, noVoyage );
        new InspectClaimedCargo( cargo, handlingEvent ).inspect();

        assertDelivery( CLAIM, MELBOURNE, DAY20, noVoyage,
                        CLAIMED, notArrived,
                        MISROUTED, directed, unknownETA, unknownLeg,
                        unknownNextHandlingEvent );
    }

    @Test
    public void step_2_Routed_ClaimInMidpointLocation() throws Exception
    {
        // Assign satisfying route going to Stockholm
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg3 ) );

        // Claim in midpoint before arrival at final destination
        // Should this really be considered misdirected?!
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY9, DAY9, trackingId, CLAIM, DALLAS, noVoyage );
        new InspectClaimedCargo( cargo, handlingEvent ).inspect();

        assertDelivery( CLAIM, DALLAS, DAY9, noVoyage,
                        CLAIMED, notArrived,
                        ROUTED, misdirected, itinerary.eta(), leg3,
                        unknownNextHandlingEvent );
    }

    @Test
    public void step_2_Routed_ClaimInFinalDestination() throws Exception
    {
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg5 ) );

        // Claim in final destination
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY24, DAY24, trackingId, CLAIM, STOCKHOLM, noVoyage );
        new InspectClaimedCargo( cargo, handlingEvent ).inspect();

        assertDelivery( CLAIM, STOCKHOLM, DAY24, noVoyage,
                        CLAIMED, notArrived,
                        ROUTED, directed, itinerary.eta(), leg5,
                        unknownNextHandlingEvent );
    }
}
