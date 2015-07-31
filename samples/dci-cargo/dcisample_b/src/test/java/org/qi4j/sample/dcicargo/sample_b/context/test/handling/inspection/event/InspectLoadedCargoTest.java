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
package org.qi4j.sample.dcicargo.sample_b.context.test.handling.inspection.event;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.sample.dcicargo.sample_b.bootstrap.test.TestApplication;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.event.InspectLoadedCargo;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.CargoHijackedException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.CargoMisdirectedException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.CargoMisroutedException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.CargoNotRoutedException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.InspectionFailedException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.UnexpectedCarrierException;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.CargoAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.HandlingEventAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.Delivery;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Leg;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.MISROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.NOT_ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.ONBOARD_CARRIER;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.LOAD;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.UNLOAD;

/**
 * {@link InspectLoadedCargo} tests
 *
 * FIXME: Every test method call the one above to allow ordered execution, ie. tests are not indepedants !
 */
public class InspectLoadedCargoTest extends TestApplication
{
    private HandlingEventAggregateRoot HANDLING_EVENTS;
    private CargoAggregateRoot CARGOS;

    @Before
    public void prepareTest()
        throws Exception
    {
        super.prepareTest();
        UnitOfWork uow = module.currentUnitOfWork();
        CARGOS = uow.get( CargoAggregateRoot.class, CargoAggregateRoot.CARGOS_ID );
        HANDLING_EVENTS = uow.get( HandlingEventAggregateRoot.class, HandlingEventAggregateRoot.HANDLING_EVENTS_ID );
        // Create new cargo
        RouteSpecification routeSpec = routeSpecFactory.build( HONGKONG, STOCKHOLM, TODAY, deadline = DAY24 );
        Delivery delivery = delivery( TODAY, IN_PORT, ROUTED, leg1 );
        cargo = CARGOS.createCargo( routeSpec, delivery, "Loaded_CARGO" );
        trackingId = cargo.trackingId().get();
//        delivery = cargo.delivery().get();
    }

    @Test
    public void deviation_2a_WrongCarrierSchedule()
        throws Exception
    {
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg1 ) );

        // V202 doesn't expect a load in Hongkong - can't determine much more before we get a correct voyage schedule
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, LOAD, HONGKONG, V202 );
        try
        {
            new InspectLoadedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch( UnexpectedCarrierException e )
        {
            assertMessage( e, "Carrier of voyage V202 didn't expect a load in Hongkong (CNHKG)" );
            assertDelivery( LOAD, HONGKONG, DAY1, V202,
                            ONBOARD_CARRIER, notArrived,
                            ROUTED, misdirected, unknownETA, unknownLeg,
                            unknownNextHandlingEvent );
        }
    }

    @Test
    public void deviation_2a_CarrierOnTime_ArrivalDate_Planned()
        throws Exception
    {
        deviation_2a_WrongCarrierSchedule();

        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg1 ) );

        //
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, LOAD, HONGKONG, V201 );
        new InspectLoadedCargo( cargo, handlingEvent ).inspect();
        assertDelivery( LOAD, HONGKONG, DAY1, V201,
                        ONBOARD_CARRIER, notArrived,
                        ROUTED, directed, itinerary.eta(), leg1,
                        UNLOAD, CHICAGO, DAY5, V201 );   // Arrival date 1 is planned
    }

    @Test
    public void deviation_2a_CarrierDelayed_ArrivalDate_Estimated()
        throws Exception
    {
        deviation_2a_CarrierOnTime_ArrivalDate_Planned();

        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg1 ) );

        //
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY2, DAY2, trackingId, LOAD, HONGKONG, V201 );
        new InspectLoadedCargo( cargo, handlingEvent ).inspect();
        assertDelivery( LOAD, HONGKONG, DAY2, V201,
                        ONBOARD_CARRIER, notArrived,
                        ROUTED, directed, itinerary.eta(), leg1,
                        UNLOAD, CHICAGO, DAY6, V201 );  // Arrival date has been postponed 1 day
    }

    @Test
    public void deviation_3a_NotRouted_MissingItinerary()
        throws Exception
    {
        deviation_2a_CarrierDelayed_ArrivalDate_Estimated();

        // Cargo not routed
        cargo.itinerary().set( null );
        cargo.delivery().set( delivery( TODAY, IN_PORT, NOT_ROUTED, leg1 ) );

        // Load cargo in Hong Kong (without an itinerary!)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, LOAD, HONGKONG, V201 );
        try
        {
            new InspectLoadedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch( CargoNotRoutedException e )
        {
            assertMessage( e, "NOT ROUTED while being handled!" );
            assertDelivery( LOAD, HONGKONG, DAY1, V201,
                            ONBOARD_CARRIER, notArrived,
                            NOT_ROUTED, directed, unknownETA, unknownLeg,
                            UNLOAD, CHICAGO, DAY5, V201 );
        }
    }

    @Test
    public void deviation_3b_Misrouted_LoadLocationOfWrongItinerary_Origin()
        throws Exception
    {
        deviation_3a_NotRouted_MissingItinerary();

        // Misroute cargo - assign unsatisfying itinerary not going to Stockholm
        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, MISROUTED, leg1 ) );

        // Load cargo in Hong Kong (with wrong itinerary)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, LOAD, HONGKONG, V201 );
        try
        {
            // Load in any location is unexpected when itinerary is wrong
            new InspectLoadedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch( CargoMisroutedException e )
        {
            assertMessage( e, "MISROUTED! Route specification is not satisfied with itinerary" );
            assertDelivery( LOAD, HONGKONG, DAY1, V201,
                            ONBOARD_CARRIER, notArrived,
                            MISROUTED, directed, unknownETA, unknownLeg,
                            UNLOAD, CHICAGO, DAY5, V201 );
        }
    }

    @Test
    public void deviation_3b_Misrouted_LoadLocationOfWrongItinerary_Midpoint()
        throws Exception
    {
        deviation_3b_Misrouted_LoadLocationOfWrongItinerary_Origin();

        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, MISROUTED, leg1 ) );

        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, LOAD, NEWYORK, V201 );
        thrown.expect( CargoMisroutedException.class, "MISROUTED! Route specification is not satisfied with itinerary" );
        new InspectLoadedCargo( cargo, handlingEvent ).inspect();
    }

    @Test
    public void deviation_3b_Misrouted_LoadLocationOfWrongItinerary_UnplannedLocation()
        throws Exception
    {
        deviation_3b_Misrouted_LoadLocationOfWrongItinerary_Midpoint();

        cargo.itinerary().set( wrongItinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, MISROUTED, leg1 ) );

        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, LOAD, ROTTERDAM, V205 );
        thrown.expect( CargoMisroutedException.class, "MISROUTED! Route specification is not satisfied with itinerary" );
        new InspectLoadedCargo( cargo, handlingEvent ).inspect();
    }

    @Test
    public void step_3_Routed()
        throws Exception
    {
        deviation_3b_Misrouted_LoadLocationOfWrongItinerary_UnplannedLocation();

        // Assign satisfying route going to Stockholm
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg1 ) );

        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, LOAD, HONGKONG, V201 );
        new InspectLoadedCargo( cargo, handlingEvent ).inspect();

        assertDelivery( LOAD, HONGKONG, DAY1, V201,
                        ONBOARD_CARRIER, notArrived,
                        ROUTED, directed, itinerary.eta(), leg1,
                        UNLOAD, CHICAGO, DAY5, V201 );
    }

    @Test
    public void deviation_4x_InternalError_InvalidItineraryProgressIndex()
        throws Exception
    {
        step_3_Routed();

        cargo.itinerary().set( itinerary );
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, LOAD, HONGKONG, V201 );

        Integer badLegIndex = 7;
        cargo.delivery().set( delivery( handlingEvent, ONBOARD_CARRIER, notArrived,
                                        ROUTED, directed, unknownETA, badLegIndex, unknownNextHandlingEvent ) );
        try
        {
            new InspectLoadedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch( InspectionFailedException e )
        {
            assertMessage( e, "INTERNAL ERROR: Itinerary progress index '7' is invalid!" );
            assertDelivery( LOAD, HONGKONG, DAY1, V201,
                            ONBOARD_CARRIER, notArrived,
                            ROUTED, directed, unknownETA, badLegIndex, unknownNextHandlingEvent );
        }
    }

    @Test
    public void deviation_4a_Misdirected_UnexpectedLoadLocation()
        throws Exception
    {
        deviation_4x_InternalError_InvalidItineraryProgressIndex();

        // Move the cargo ahead on the route. Third leg of itinerary expects load in Dallas.
        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg3 ) );

        // Unexpected load in previous load location of itinerary (onto expected voyage) - can't go back in time.
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY7, DAY5, trackingId, LOAD, ROTTERDAM, V202 );
        try
        {
            new InspectLoadedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch( CargoMisdirectedException e )
        {
            assertMessage( e, "MISDIRECTED! Itinerary expected load in New York (USNYC)" );
            assertDelivery( LOAD, ROTTERDAM, DAY5, V202,    // Itinerary expected: LOAD, NEWYORK, DAY7, V202
                            ONBOARD_CARRIER, notArrived,
                            ROUTED, misdirected, itinerary.eta(), leg3,
                            unknownNextHandlingEvent );     // When location is wrong we have to investigate...
        }
    }

    @Test
    public void deviation_4b_Misdirected_UnexpectedLoadVoyage_PreviousInItinerary()
        throws Exception
    {
        deviation_4a_Misdirected_UnexpectedLoadLocation();

        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg3 ) );

        // Unexpected load onto previous voyage (in expected location) - can't go back in time.
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY7, DAY7, trackingId, LOAD, NEWYORK, V201 );
        try
        {
            new InspectLoadedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch( CargoMisdirectedException e )
        {
            assertMessage( e, "MISDIRECTED! Itinerary expected load onto voyage V202" );
            assertDelivery( LOAD, NEWYORK, DAY7, V201,          // Itinerary expected: LOAD, NEWYORK, DAY7, V202
                            ONBOARD_CARRIER, notArrived,
                            ROUTED, misdirected, itinerary.eta(), leg3,
                            UNLOAD, GOTHENBURG, DAY13, V201 );  // Itinerary expected: UNLOAD, DALLAS, DAY8, V202
        }
    }

    @Test
    public void deviation_4b_Misdirected_UnexpectedLoadVoyage_NextInItinerary()
        throws Exception
    {
        deviation_4b_Misdirected_UnexpectedLoadVoyage_PreviousInItinerary();

        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg3 ) );

        // Unexpected load onto future voyage (in expected location) - can't jump ahead in route plan.
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY7, DAY7, trackingId, LOAD, NEWYORK, V203 );
        thrown.expect( CargoMisdirectedException.class, "MISDIRECTED! Itinerary expected load onto voyage V202" );
        new InspectLoadedCargo( cargo, handlingEvent ).inspect();
    }

    @Test
    public void deviation_4b_Misdirected_UnexpectedLoadVoyage_VoyageNotInItinerary()
        throws Exception
    {
        deviation_4b_Misdirected_UnexpectedLoadVoyage_NextInItinerary();

        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg3 ) );

        // Unexpected load onto voyage not in itinerary
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY7, DAY7, trackingId, LOAD, NEWYORK, V204 );
        thrown.expect( CargoMisdirectedException.class, "MISDIRECTED! Itinerary expected load onto voyage V202" );
        new InspectLoadedCargo( cargo, handlingEvent ).inspect();
    }

    @Test
    public void deviation_4c_Misdirected_UnexpectedLoadVoyage_Unplanned_ButGoingToWantedLocation()
        throws Exception
    {
        deviation_4b_Misdirected_UnexpectedLoadVoyage_VoyageNotInItinerary();

        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg3 ) );

        // Unexpected load onto voyage not in itinerary - but the carrier is going to our expected arrival location!
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY9, DAY9, trackingId, LOAD, NEWYORK, V205 );
        try
        {
            new InspectLoadedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch( CargoMisdirectedException e )
        {
            assertMessage( e, "MISDIRECTED! Cargo is heading to expected arrival location USDAL but on unexpected voyage V205" );
            assertDelivery( LOAD, NEWYORK, DAY9, V205,          // Itinerary expected: LOAD, NEWYORK, DAY7, V202
                            ONBOARD_CARRIER, notArrived,
                            ROUTED, misdirected, itinerary.eta(), leg3,
                            UNLOAD, DALLAS, DAY10, V205 );      // Itinerary expected: UNLOAD, DALLAS, DAY8, V202
        }
    }

    @Test
    public void deviation_4d_Misdirected_ExpectedLoadVoyage_UnexpectedNewVoyageSchedule()
        throws Exception
    {
        deviation_4c_Misdirected_UnexpectedLoadVoyage_Unplanned_ButGoingToWantedLocation();

        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg3 ) );

        // Unexpected voyage schedule change
        V202 = voyage( "V202", schedule(
            carrierMovement( CHICAGO, NEWYORK, DAY3, DAY5 ),
            carrierMovement( NEWYORK, HAMBURG, DAY7, DAY15 ),
            carrierMovement( HAMBURG, ROTTERDAM, DAY16, DAY17 ),
            carrierMovement( ROTTERDAM, GOTHENBURG, DAY17, DAY19 )
        ) );

        // Expected load onto voyage - but carrier has changed arrival location!
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY7, DAY7, trackingId, LOAD, NEWYORK, V202 );
        try
        {
            new InspectLoadedCargo( cargo, handlingEvent ).inspect();
            fail();
        }
        catch( CargoMisdirectedException e )
        {
            assertMessage( e, "MISDIRECTED! Itinerary expects voyage V202 to arrive in USDAL but carrier is now going to DEHAM" );
            assertDelivery( LOAD, NEWYORK, DAY7, V202,          // Itinerary expected: LOAD, NEWYORK, DAY7, V202
                            ONBOARD_CARRIER, notArrived,
                            ROUTED, misdirected, itinerary.eta(), leg3,
                            UNLOAD, HAMBURG, DAY15, V202 );     // Itinerary expected: UNLOAD, DALLAS, DAY8, V202
        }
    }

    @Test
    public void success_Load()
        throws Exception
    {
        deviation_4d_Misdirected_ExpectedLoadVoyage_UnexpectedNewVoyageSchedule();

        cargo.itinerary().set( itinerary );
        cargo.delivery().set( delivery( TODAY, IN_PORT, ROUTED, leg3 ) );

        // Restore expected voyage schedule change
        V202 = voyage( "V202", schedule(
            carrierMovement( CHICAGO, NEWYORK, DAY3, DAY5 ),
            carrierMovement( NEWYORK, DALLAS, DAY7, DAY8 ),
            carrierMovement( DALLAS, ROTTERDAM, DAY10, DAY17 ),
            carrierMovement( ROTTERDAM, GOTHENBURG, DAY17, DAY19 )
        ) );

        // Expected load (leg 3)
        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY7, DAY7, trackingId, LOAD, NEWYORK, V202 );
        new InspectLoadedCargo( cargo, handlingEvent ).inspect();

        Leg currentCarrierMovement = itinerary.leg( delivery.itineraryProgressIndex().get() );
        assertThat( currentCarrierMovement.unloadLocation().get(), is( equalTo( DALLAS ) ) );
        assertThat( currentCarrierMovement.unloadTime().get(), is( equalTo( DAY8 ) ) );
        assertThat( currentCarrierMovement.voyage().get(), is( equalTo( V202 ) ) );

        assertDelivery( LOAD, NEWYORK, DAY7, V202,
                        ONBOARD_CARRIER, notArrived,
                        ROUTED, directed, itinerary.eta(), leg3,
                        UNLOAD, DALLAS, DAY8, V202 );
    }

    @Test
    public void riskZoneDestination()
        throws Exception
    {
        success_Load();

        // Risk zone destination
        routeSpec = routeSpecFactory.build( HANGZHOU, ROTTERDAM, new Date(), deadline = DAY24 );
        delivery = delivery( TODAY, ONBOARD_CARRIER, ROUTED, leg1 );
        cargo = CARGOS.createCargo( routeSpec, delivery, "Naive" );
        trackingId = cargo.trackingId().get();
        itinerary = itinerary(
            leg( V205, HANGZHOU, MOGADISHU, DAY1, DAY2 ),
            leg( V205, MOGADISHU, ROTTERDAM, DAY2, DAY4 )
        );
        cargo.itinerary().set( itinerary );

        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY1, DAY1, trackingId, LOAD, HANGZHOU, V205 );
        cargo.delivery().set( delivery( handlingEvent, ONBOARD_CARRIER, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg1,
                                        nextHandlingEvent( UNLOAD, MOGADISHU, DAY2, V205 ) ) );

        assertDelivery( LOAD, HANGZHOU, DAY1, V205,
                        ONBOARD_CARRIER, notArrived,
                        ROUTED, directed, itinerary.eta(), leg1,
                        UNLOAD, MOGADISHU, DAY2, V205 );
        try
        {
            new InspectLoadedCargo( cargo, handlingEvent ).inspect();
        }
        catch( CargoHijackedException e )
        {
            assertMessage( e, "Cargo 'Naive' was hijacked." );
            assertDelivery( LOAD, HANGZHOU, DAY1, V205,
                            TransportStatus.UNKNOWN, notArrived,
                            ROUTED, directed, itinerary.eta(), leg1,
                            UNLOAD, MOGADISHU, DAY2, V205 );
        }
    }

    @Test
    public void riskZoneDeparture()
        throws Exception
    {
        riskZoneDestination();

        // Risk zone departure (they know you now, so risk is higher)
        cargo = CARGOS.createCargo( routeSpec, delivery, "Hopeful" );
        trackingId = cargo.trackingId().get();
        cargo.itinerary().set( itinerary ); // Risky itinerary

        handlingEvent = HANDLING_EVENTS.createHandlingEvent( DAY2, DAY2, trackingId, LOAD, MOGADISHU, V205 );
        cargo.delivery().set( delivery( handlingEvent, ONBOARD_CARRIER, notArrived,
                                        ROUTED, directed, itinerary.eta(), leg2,
                                        nextHandlingEvent( UNLOAD, ROTTERDAM, DAY4, V205 ) ) );

        assertDelivery( LOAD, MOGADISHU, DAY2, V205,
                        ONBOARD_CARRIER, notArrived,
                        ROUTED, directed, itinerary.eta(), leg2,
                        UNLOAD, ROTTERDAM, DAY4, V205 );
        try
        {
            new InspectLoadedCargo( cargo, handlingEvent ).inspect();
        }
        catch( CargoHijackedException e )
        {
            assertMessage( e, "Cargo 'Hopeful' was hijacked." );
            assertDelivery( LOAD, MOGADISHU, DAY2, V205,
                            TransportStatus.UNKNOWN, notArrived,
                            ROUTED, directed, itinerary.eta(), leg2,
                            UNLOAD, ROTTERDAM, DAY4, V205 );
        }
    }
}
