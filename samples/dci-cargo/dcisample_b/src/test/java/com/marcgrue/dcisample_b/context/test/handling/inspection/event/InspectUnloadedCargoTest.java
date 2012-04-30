package com.marcgrue.dcisample_b.context.test.handling.inspection.event;

import com.marcgrue.dcisample_b.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.event.InspectUnloadedCargo;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.CargoMisdirectedException;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.CargoMisroutedException;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.CargoNotRoutedException;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.InspectionFailedException;
import com.marcgrue.dcisample_b.data.structure.itinerary.Leg;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.*;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.ONBOARD_CARRIER;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.LOAD;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.UNLOAD;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link InspectUnloadedCargo} tests
 */
public class InspectUnloadedCargoTest extends TestApplication
{
    @BeforeClass
    public static void setup() throws Exception
    {
        TestApplication.setup();

        // Create new cargo
        routeSpec = routeSpecFactory.build( HONGKONG, STOCKHOLM, new Date(), deadline = DAY24 );
        delivery = delivery( TODAY, ONBOARD_CARRIER, ROUTED, leg1 );
        cargo = CARGOS.createCargo( routeSpec, delivery, "Unloaded_CARGO" );
        trackingId = cargo.trackingId().get();
    }

    @Test
    public void precondition_CannotInspectUnloadInDestinationHere() throws Exception
    {
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, UNLOAD, STOCKHOLM, V205 );
        try
        {
            new InspectUnloadedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch (InspectionFailedException e)
        {
            assertMessage( e, "INTERNAL ERROR: Can only inspect unloaded cargo that hasn't arrived at destination" );
        }
    }

    @Test
    public void deviation_2a_NotRouted() throws Exception
    {
        // Cargo not routed
        cargo.itinerary().set( null );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, NOT_ROUTED, leg1 ) );

        // Unload in Chicago (without an itinerary!)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY5, DAY5, trackingId, UNLOAD, CHICAGO, V201 );
        try
        {
            new InspectUnloadedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch (CargoNotRoutedException e)
        {
            assertMessage( e, "NOT ROUTED while being handled!" );

            // An unexpected unload shouldn't be considered an itinerary progress - legIndex stays unchanged
            assertDelivery( UNLOAD, CHICAGO, DAY5, V201,
                            IN_PORT, notArrived,
                            NOT_ROUTED, directed, unknownETA, unknownLeg,
                            unknownNextHandlingEvent );
        }
    }

    @Test
    public void deviation_2b_Misrouted_UnloadLocationOfWrongItinerary_Origin() throws Exception
    {
        // Misroute cargo - assign unsatisfying itinerary not going to Stockholm
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, MISROUTED, leg1 ) );

        // Unload in Hong Kong (with wrong itinerary)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, UNLOAD, HONGKONG, V201 );
        try
        {
            new InspectUnloadedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch (CargoMisroutedException e)
        {
            assertMessage( e, "MISROUTED! Route specification is not satisfied with itinerary" );

            // An unexpected unload shouldn't be considered an itinerary progress - legIndex stays unchanged
            assertDelivery( UNLOAD, HONGKONG, DAY1, V201,
                            IN_PORT, notArrived,
                            MISROUTED, directed, unknownETA, unknownLeg,
                            unknownNextHandlingEvent );
        }
    }

    @Test
    public void deviation_2b_Misrouted_UnloadLocationOfWrongItinerary_OtherItineraryLocation() throws Exception
    {
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, MISROUTED, leg1 ) );

        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, UNLOAD, NEWYORK, V201 );
        thrown.expect( CargoMisroutedException.class, "MISROUTED! Route specification is not satisfied with itinerary" );
        new InspectUnloadedCargo( cargo, handlingEvent ).inspect();
    }

    @Test
    public void deviation_2b_Misrouted_UnloadLocationOfWrongItinerary_UnplannedLocation() throws Exception
    {
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, MISROUTED, leg1 ) );

        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, UNLOAD, ROTTERDAM, V204 );
        thrown.expect( CargoMisroutedException.class, "MISROUTED! Route specification is not satisfied with itinerary" );
        new InspectUnloadedCargo( cargo, handlingEvent ).inspect();
    }


    @Test
    public void step_2_Routed_UnloadedInPlannedLocation() throws Exception
    {
        // Assign satisfying route going to Stockholm
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, ROUTED, leg1 ) );

        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY5, DAY5, trackingId, UNLOAD, CHICAGO, V201 );
        new InspectUnloadedCargo( cargo, handlingEvent ).inspect();

        // Itinerary progresses to next leg
        assertDelivery( UNLOAD, CHICAGO, DAY5, V201,
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary.eta(),leg2,
                        LOAD, CHICAGO, DAY5, V201 );
    }

    @Test
    public void deviation_3x_InternalError_InvalidItineraryProgressIndex() throws Exception
    {
        cargo.itinerary().set( itinerary );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY5, DAY5, trackingId, UNLOAD, CHICAGO, V201 );

        Integer badLegIndex = 7;
        cargo.delivery().set( delivery( handlingEvent, IN_PORT, notArrived,
                                        ROUTED, directed, unknownETA, badLegIndex,
                                        unknownNextHandlingEvent ) );
        try
        {
            new InspectUnloadedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch (InspectionFailedException e)
        {
            assertMessage( e, "INTERNAL ERROR: Itinerary progress index '7' is invalid!" );
            assertDelivery( UNLOAD, CHICAGO, DAY5, V201,
                            IN_PORT, notArrived,
                            ROUTED, directed, unknownETA, badLegIndex,
                            unknownNextHandlingEvent );
        }
    }

    @Test
    public void deviation_3a_ReRouted_UnloadInNewOrigin() throws Exception
    {
        // Re-route with new satisfying itinerary
        cargo.itinerary().set( itinerary );

        // Cargo was re-route on board a carrier
        cargo.delivery().set( delivery( null, ONBOARD_CARRIER, notArrived,
                                        ROUTED, misdirected, itinerary.eta(), leg3,
                                        nextHandlingEvent( UNLOAD, HONGKONG, DAY6, V201 ) ) );

        // Unload in new route specification origin (load location of itinerary leg 1)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, UNLOAD, HONGKONG, V204 );
        new InspectUnloadedCargo( cargo, handlingEvent ).inspect();

        // Itinerary progress starts over from leg 1 again
        assertDelivery( UNLOAD, HONGKONG, DAY1, V204,
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary.eta(),leg1,
                        LOAD, HONGKONG, DAY1, V201 );
    }

    @Test
    public void deviation_3b_Misdirected_UnexpectedUnloadLocation_Origin() throws Exception
    {
        // Going fine so far
        cargo.itinerary().set( itinerary );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY5, DAY5, trackingId, LOAD, CHICAGO, V201 );
        cargo.delivery().set( delivery( handlingEvent, ONBOARD_CARRIER, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg2,
                                        nextHandlingEvent( UNLOAD, NEWYORK, DAY6, V201 ) ) );

        // Unexpected unload in origin
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY6, DAY6, trackingId, UNLOAD, HONGKONG, V201 );
        new InspectUnloadedCargo( cargo, handlingEvent ).inspect();

        // Itinerary progress starts over from leg 1 again
        assertDelivery( UNLOAD, HONGKONG, DAY6, V201,
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary.eta(),leg1,
                        LOAD, HONGKONG, DAY1, V201 );
    }

    @Test
    public void deviation_3b_Misdirected_UnexpectedUnloadLocation_PreviousInItinerary() throws Exception
    {
        // Move the cargo ahead on the route. Third leg of itinerary expects unload in Dallas.
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, ROUTED, leg3 ) );

        // Unexpected unload in previous unload location of itinerary.
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY7, DAY7, trackingId, UNLOAD, NEWYORK, V201 );
        try
        {
            new InspectUnloadedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch (CargoMisdirectedException e)
        {
            assertMessage( e, "MISDIRECTED! Itinerary expected unload in USDAL" );
            assertDelivery( UNLOAD, NEWYORK, DAY7, V201,      // Itinerary expected: UNLOAD, DALLAS, DAY8, V202
                            IN_PORT, notArrived,
                            ROUTED, misdirected, itinerary.eta(), leg3,
                            unknownNextHandlingEvent );
        }
    }

    @Test
    public void deviation_3b_Misdirected_UnexpectedUnloadLocation_NextInItinerary() throws Exception
    {
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, ROUTED, leg3 ) );

        // Unexpected load in next load location of itinerary (onto expected voyage) - can't jump ahead in route plan.
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY7, DAY7, trackingId, UNLOAD, GOTHENBURG, V202 );
        thrown.expect( CargoMisdirectedException.class, "MISDIRECTED! Itinerary expected unload in USDAL" );
        new InspectUnloadedCargo( cargo, handlingEvent ).inspect();
    }

    @Test
    public void deviation_3b_Misdirected_UnexpectedUnloadLocation_Unplanned() throws Exception
    {
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, ROUTED, leg3 ) );

        // Unexpected load in unplanned location (onto expected voyage)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY7, DAY7, trackingId, UNLOAD, HAMBURG, V202 );
        thrown.expect( CargoMisdirectedException.class, "MISDIRECTED! Itinerary expected unload in USDAL" );
        new InspectUnloadedCargo( cargo, handlingEvent ).inspect();
    }

    @Test
    public void deviation_3c_ExpectedUnloadLocation_UnexpectedUnloadVoyage() throws Exception
    {
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, ROUTED, leg3 ) );

        // Unload in expected location but from unexpected voyage - do we care? For now not.
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY10, DAY10, trackingId, UNLOAD, DALLAS, V205 );
        new InspectUnloadedCargo( cargo, handlingEvent ).inspect();

        // Itinerary should have progressed to leg 4
        assertDelivery( UNLOAD, DALLAS, DAY10, V205,      // Itinerary expected: UNLOAD, DALLAS, DAY8, V202
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary.eta(),leg4,
                        LOAD, DALLAS, DAY10, V202 ); // leg 4 load location
    }

    @Test
    public void success_Unload() throws Exception
    {
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, ONBOARD_CARRIER, ROUTED, leg4 ) );

        // Expected unload in leg 4 unload location (Rotterdam)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY17, DAY17, trackingId, UNLOAD, ROTTERDAM, V202 );
        new InspectUnloadedCargo( cargo, handlingEvent ).inspect();

        // Itinerary should have progressed to leg 5
        Leg nextCarrierMovement = itinerary.leg( cargo.delivery().get().itineraryProgressIndex().get() );
        assertThat( nextCarrierMovement.loadLocation().get(), is( equalTo( ROTTERDAM ) ) );
        assertThat( nextCarrierMovement.loadTime().get(), is( equalTo( DAY20 ) ) );
        assertThat( nextCarrierMovement.voyage().get(), is( equalTo( V203 ) ) );

        assertDelivery( UNLOAD, ROTTERDAM, DAY17, V202,
                        IN_PORT, notArrived,
                        ROUTED, directed, itinerary.eta(),leg5,
                        LOAD, ROTTERDAM, DAY20, V203 ); // leg 5 load location
    }
}
