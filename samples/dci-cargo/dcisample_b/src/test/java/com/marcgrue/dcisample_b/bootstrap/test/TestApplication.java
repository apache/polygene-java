package com.marcgrue.dcisample_b.bootstrap.test;

import com.marcgrue.dcisample_b.bootstrap.sampledata.BaseData;
import com.marcgrue.dcisample_b.data.factory.RouteSpecificationFactoryService;
import com.marcgrue.dcisample_b.data.structure.cargo.Cargo;
import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.delivery.Delivery;
import com.marcgrue.dcisample_b.data.structure.delivery.NextHandlingEvent;
import com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus;
import com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;
import com.marcgrue.dcisample_b.data.structure.location.Location;
import com.marcgrue.dcisample_b.data.structure.tracking.TrackingId;
import com.marcgrue.dcisample_b.data.structure.voyage.Voyage;
import com.marcgrue.dcisample_b.infrastructure.dci.Context;
import com.marcgrue.dcisample_b.infrastructure.testing.ExpectedException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.structure.ApplicationSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Base class for testing Context Interactions
 */
public class TestApplication
      extends BaseData
{
    // Logger for sub classes
    protected Logger logger = LoggerFactory.getLogger( getClass() );

    protected static ApplicationSPI app;
    protected static Module module;
    protected static UnitOfWorkFactory uowf;
    protected static TransientBuilderFactory tbf;
    protected static QueryBuilderFactory qbf;

    protected static RouteSpecificationFactoryService routeSpecFactory;

    final protected static Date TODAY = new Date();
    final protected static Date DAY1 = day( 1 );
    final protected static Date DAY2 = day( 2 );
    final protected static Date DAY3 = day( 3 );
    final protected static Date DAY4 = day( 4 );
    final protected static Date DAY5 = day( 5 );
    final protected static Date DAY6 = day( 6 );
    final protected static Date DAY7 = day( 7 );
    final protected static Date DAY8 = day( 8 );
    final protected static Date DAY9 = day( 9 );
    final protected static Date DAY10 = day( 10 );
    final protected static Date DAY11 = day( 11 );
    final protected static Date DAY12 = day( 12 );
    final protected static Date DAY13 = day( 13 );
    final protected static Date DAY14 = day( 14 );
    final protected static Date DAY15 = day( 15 );
    final protected static Date DAY16 = day( 16 );
    final protected static Date DAY17 = day( 17 );
    final protected static Date DAY18 = day( 18 );
    final protected static Date DAY19 = day( 19 );
    final protected static Date DAY20 = day( 20 );
    final protected static Date DAY21 = day( 21 );
    final protected static Date DAY22 = day( 22 );
    final protected static Date DAY23 = day( 23 );
    final protected static Date DAY24 = day( 24 );
    final protected static Date DAY25 = day( 25 );

    protected static Voyage V201;
    protected static Voyage V202;
    protected static Voyage V203;
    protected static Voyage V204;
    protected static Voyage V205;

    final protected static Voyage noVoyage = null;
    final protected static boolean notArrived = false;
    final protected static boolean arrived = true;
    final protected static boolean directed = false;
    final protected static boolean misdirected = true;
    final protected static Date unknownETA = null;
    final protected static Date noSpecificDate = null;
    final protected static Integer leg1 = 0;
    final protected static Integer leg2 = 1;
    final protected static Integer leg3 = 2;
    final protected static Integer leg4 = 3;
    final protected static Integer leg5 = 4;
    final protected static Integer unknownLeg = 0;
    final protected static NextHandlingEvent unknownNextHandlingEvent = null;

    protected static Date deadline;
    protected static Date arrival;
    protected static RouteSpecification routeSpec;
    protected static RouteSpecification newRouteSpec;
    protected static Delivery delivery;
    protected static Cargo cargo;
    protected static TrackingId trackingId;
    protected static String trackingIdString;
    protected static Itinerary itinerary;
    protected static Itinerary wrongItinerary;
    protected static HandlingEvent handlingEvent;

    @BeforeClass
    public static void setup() throws Exception
    {
        System.out.println( "\n@@@@@@@@@@@  TEST SUITE  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" );
        app = new Energy4Java().newApplication( new TestAssembler() );
        app.activate();

        module = app.findModule( "BOOTSTRAP", "BOOTSTRAP-Bootstrap" );
        uowf = module.unitOfWorkFactory();
        tbf = module.transientBuilderFactory();
        qbf = module.queryBuilderFactory();

        ServiceReference<RouteSpecificationFactoryService> routeSpecFactoryServiceRef =
              module.serviceFinder().findService( RouteSpecificationFactoryService.class );
        routeSpecFactory = routeSpecFactoryServiceRef.get();

        Context.prepareContextBaseClass( uowf, vbf );

        populateTestData();

        // Separate test suites in console output
        System.out.println();
    }

    // Allow to test message output from exceptions after they have been thrown
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // Print current test method name to console
    @Rule
    public TestName name = new TestName();

    @Before
    public void printCurrentTestMethodName()
    {
        logger.info( name.getMethodName() );
    }

    public void assertMessage( Exception e, String msg )
    {
        String message = "\nEXPECTED: " + msg + "\nGOT: " + e.getMessage();
        assertTrue( message, e.getMessage().contains( msg ) );
    }


    @AfterClass
    public static void tearDown() throws Exception
    {
        if (uow != null)
        {
            uow.discard();
            uow = null;
        }

        if (uowf != null && uowf.currentUnitOfWork() != null)
        {
            UnitOfWork current;
            while (( current = uowf.currentUnitOfWork() ) != null)
            {
                if (current.isOpen())
                {
                    current.discard();
                }
                else
                {
                    throw new InternalError( "UoW is on the stack, but not opened." );
                }
            }

            new Exception( "UnitOfWork not properly cleaned up" ).printStackTrace();
        }

        if (app != null)
        {
            app.passivate();
        }
    }

    private static void populateTestData() throws Exception
    {
        // UnLocode value objects
        AUMEL = unlocode( "AUMEL" ); // Melbourne
        CNHGH = unlocode( "CNHGH" ); // Hangzou
        CNHKG = unlocode( "CNHKG" ); // Hong Kong
        CNSHA = unlocode( "CNSHA" ); // Shanghai
        DEHAM = unlocode( "DEHAM" ); // Hamburg
        FIHEL = unlocode( "FIHEL" ); // Helsinki
        JNTKO = unlocode( "JNTKO" ); // Tokyo
        NLRTM = unlocode( "NLRTM" ); // Rotterdam
        SEGOT = unlocode( "SEGOT" ); // Gothenburg
        SESTO = unlocode( "SESTO" ); // Stockholm
        SOMGQ = unlocode( "SOMGQ" ); // Mogadishu
        USCHI = unlocode( "USCHI" ); // Chicago
        USDAL = unlocode( "USDAL" ); // Dallas
        USNYC = unlocode( "USNYC" ); // New York

        // Get locations created in BaseDataService on startup
        MELBOURNE = uow.get( Location.class, "AUMEL" );
        HANGZHOU = uow.get( Location.class, "CNHGH" );
        HONGKONG = uow.get( Location.class, "CNHKG" );
        SHANGHAI = uow.get( Location.class, "CNSHA" );
        HAMBURG = uow.get( Location.class, "DEHAM" );
        HELSINKI = uow.get( Location.class, "FIHEL" );
        TOKYO = uow.get( Location.class, "JNTKO" );
        ROTTERDAM = uow.get( Location.class, "NLRTM" );
        GOTHENBURG = uow.get( Location.class, "SEGOT" );
        STOCKHOLM = uow.get( Location.class, "SESTO" );
        MOGADISHU = uow.get( Location.class, "SOMGQ" );
        CHICAGO = uow.get( Location.class, "USCHI" );
        DALLAS = uow.get( Location.class, "USDAL" );
        NEWYORK = uow.get( Location.class, "USNYC" );

        // Voyage entity objects for testing
        V201 = voyage( "V201", schedule(
              carrierMovement( HONGKONG, CHICAGO, DAY1, DAY5 ),
              carrierMovement( CHICAGO, NEWYORK, DAY5, DAY6 ),
              carrierMovement( NEWYORK, GOTHENBURG, DAY6, DAY12 )
        ) );
        V202 = voyage( "V202", schedule(
              carrierMovement( CHICAGO, NEWYORK, DAY3, DAY5 ),
              carrierMovement( NEWYORK, DALLAS, DAY7, DAY8 ),
              carrierMovement( DALLAS, ROTTERDAM, DAY10, DAY17 ),
              carrierMovement( ROTTERDAM, HAMBURG, DAY17, DAY19 ),
              carrierMovement( HAMBURG, GOTHENBURG, DAY20, DAY24 )
        ) );
        V203 = voyage( "V203", schedule(
              carrierMovement( NEWYORK, HAMBURG, DAY3, DAY12 ),
              carrierMovement( HAMBURG, ROTTERDAM, DAY13, DAY18 ),
              carrierMovement( ROTTERDAM, STOCKHOLM, DAY20, DAY23 ),
              carrierMovement( STOCKHOLM, HELSINKI, DAY24, DAY25 )
        ) );
        V204 = voyage( "V204", schedule(
              carrierMovement( TOKYO, HANGZHOU, DAY3, DAY6 ),
              carrierMovement( HANGZHOU, HONGKONG, DAY7, DAY8 ),
              carrierMovement( HONGKONG, NEWYORK, DAY9, DAY12 ),
              carrierMovement( NEWYORK, MELBOURNE, DAY13, DAY19 )
        ) );
        V205 = voyage( "V205", schedule(
              carrierMovement( HANGZHOU, MOGADISHU, DAY1, DAY2 ),
              carrierMovement( MOGADISHU, ROTTERDAM, DAY2, DAY4 ),
              carrierMovement( ROTTERDAM, NEWYORK, DAY4, DAY7 ),
              carrierMovement( NEWYORK, DALLAS, DAY9, DAY10 )
        ) );

        itinerary = itinerary(
              leg( V201, HONGKONG, CHICAGO, DAY1, DAY5 ),
              leg( V201, CHICAGO, NEWYORK, DAY5, DAY6 ),
              leg( V202, NEWYORK, DALLAS, DAY7, DAY8 ),
              leg( V202, DALLAS, ROTTERDAM, DAY10, DAY17 ),
              leg( V203, ROTTERDAM, STOCKHOLM, DAY20, DAY23 )
        );

        wrongItinerary = itinerary(
              leg( V201, HONGKONG, CHICAGO, DAY1, DAY5 ),
              leg( V201, CHICAGO, NEWYORK, DAY5, DAY6 ),
              leg( V204, NEWYORK, MELBOURNE, DAY13, DAY19 )
        );
    }


    public void assertDelivery( HandlingEventType handlingEventType,
                                Location location,
                                Date completion,
                                Voyage voyage,

                                TransportStatus transportStatus,
                                Boolean isUnloadedAtDestination,

                                RoutingStatus routingStatus,
                                Boolean isMisdirected,
                                Date eta,
                                Integer itineraryProgress,

                                HandlingEventType nextHandlingEventType,
                                Location nextLocation,
                                Date nextTime,
                                Voyage nextVoyage
    ) throws Exception
    {
        delivery = cargo.delivery().get();

        // Last handling event
        if (delivery.lastHandlingEvent().get() != null
              || handlingEventType != null || location != null || completion != null || voyage != null)
        {
            assertThat( "lastHandlingEvent - handlingEventType",
                        delivery.lastHandlingEvent().get().handlingEventType().get(), is( equalTo( handlingEventType ) ) );
            assertThat( "lastHandlingEvent - location",
                        delivery.lastHandlingEvent().get().location().get(), is( equalTo( location ) ) );
            assertThat( "lastHandlingEvent - completionTime",
                        delivery.lastHandlingEvent().get().completionTime().get(), is( equalTo( completion ) ) );
            assertThat( "lastHandlingEvent - voyage",
                        delivery.lastHandlingEvent().get().voyage().get(), is( equalTo( voyage ) ) );
        }

        // Other transport status
        assertThat( "transportStatus",
                    delivery.transportStatus().get(), is( equalTo( transportStatus ) ) );
        assertThat( "isUnloadedAtDestination",
                    delivery.isUnloadedAtDestination().get(), is( equalTo( isUnloadedAtDestination ) ) );


        // Routing and direction
        assertThat( "routingStatus",
                    delivery.routingStatus().get(), is( equalTo( routingStatus ) ) );
        assertThat( "isMisdirected",
                    delivery.isMisdirected().get(), is( equalTo( isMisdirected ) ) );
        assertThat( "eta",
                    delivery.eta().get(), is( equalTo( eta ) ) );
        assertThat( "itineraryProgressIndex",
                    delivery.itineraryProgressIndex().get(), is( equalTo( itineraryProgress ) ) );


        // Next handling event
        if (nextHandlingEventType == null)
        {
            assertThat( "nextHandlingEvent - handlingEventType",
                        delivery.nextHandlingEvent().get(), is( equalTo( null ) ) );
        }
        else
        {
            assertThat( "nextHandlingEvent - handlingEventType",
                        delivery.nextHandlingEvent().get().handlingEventType().get(), is( equalTo( nextHandlingEventType ) ) );
            assertThat( "nextHandlingEvent - location",
                        delivery.nextHandlingEvent().get().location().get(), is( equalTo( nextLocation ) ) );


            if (delivery.nextHandlingEvent().get().time().get() != null)
            {
                // Estimating a new carrier arrival time might be calculated a second
                // after initial dates are set, so we skip the seconds
                SimpleDateFormat parser = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
                String calculatedTime = parser.format( delivery.nextHandlingEvent().get().time().get() );
                String expectedTime = parser.format( nextTime );
                assertThat( "nextHandlingEvent - time", calculatedTime, is( equalTo( expectedTime ) ) );
            }
            else
                assertThat( "nextHandlingEvent - time", delivery.nextHandlingEvent().get().time().get(), is( equalTo( nextTime ) ) );

            assertThat( "nextHandlingEvent - voyage",
                        delivery.nextHandlingEvent().get().voyage().get(), is( equalTo( nextVoyage ) ) );
        }
    }

    public void assertDelivery( HandlingEventType handlingEventType,
                                Location location,
                                Date completion,
                                Voyage voyage,

                                TransportStatus transportStatus,
                                Boolean isUnloadedAtDestination,

                                RoutingStatus routingStatus,
                                Boolean isMisdirected,
                                Date eta,
                                Integer itineraryProgress,

                                NextHandlingEvent noNextHandlingEvent
    ) throws Exception
    {
        assertDelivery( handlingEventType, location, completion, voyage,
                        transportStatus, isUnloadedAtDestination, routingStatus, isMisdirected, eta,
                        itineraryProgress, null, null, null, null );
    }

    public void assertRouteSpec( Location origin, Location destination, Date earliestDeparture, Date deadline )
    {
        newRouteSpec = cargo.routeSpecification().get();

        assertThat( newRouteSpec.origin().get(), is( equalTo( origin ) ) );
        assertThat( newRouteSpec.destination().get(), is( equalTo( destination ) ) );
        assertThat( newRouteSpec.earliestDeparture().get(), is( equalTo( earliestDeparture ) ) );
        assertThat( newRouteSpec.arrivalDeadline().get(), is( equalTo( deadline ) ) );
    }
}