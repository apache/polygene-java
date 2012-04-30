package com.marcgrue.dcisample_b.context.test.handling.parsing;

import com.marcgrue.dcisample_b.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_b.context.interaction.handling.parsing.ParseHandlingEventData;
import com.marcgrue.dcisample_b.context.interaction.handling.parsing.exception.InvalidHandlingEventDataException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.service.ServiceReference;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.NOT_RECEIVED;

/**
 * {@link ParseHandlingEventData} tests
 */
public class ParseHandlingEventDataTest extends TestApplication
{
    static ParseHandlingEventData handlingEventParser;
    static String completionTime;

    @BeforeClass
    public static void setup() throws Exception
    {
        TestApplication.setup();

        // Create new cargo
        routeSpec = routeSpecFactory.build( HONGKONG, STOCKHOLM, new Date(), deadline = DAY24 );
        delivery = delivery( TODAY, NOT_RECEIVED, ROUTED, unknownLeg );
        cargo = CARGOS.createCargo( routeSpec, delivery, "ABC" );
        trackingId = cargo.trackingId().get();
        trackingIdString = trackingId.id().get();
        cargo.itinerary().set( itinerary );
        completionTime = new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format( new Date( ) ) ;

        // Start ParseHandlingEventData service
        ServiceReference<ParseHandlingEventData> ParseHandlingEventDataRef =
              module.serviceFinder().findService( ParseHandlingEventData.class );
        handlingEventParser = ParseHandlingEventDataRef.get();
    }


    // Null

    @Test
    public void deviation_2a_Null_CompletionTimeString() throws Exception
    {
        thrown.expect( ConstraintViolationException.class, "constraint \"not optional(param1)\", for value 'null'" );
        handlingEventParser.parse( null, trackingIdString, "RECEIVE", "CNHKG", null );
    }

    @Test
    public void deviation_2a_Null_TrackingIdString() throws Exception
    {
        thrown.expect( ConstraintViolationException.class, "constraint \"not optional(param2)\", for value 'null'" );
        handlingEventParser.parse( completionTime, null, "RECEIVE", "CNHKG", null );
    }

    // etc...

    @Test
    public void step_2_Null_VoyageNumberString() throws Exception
    {
        // No voyage number string is ok
        handlingEventParser.parse( completionTime, trackingIdString, "RECEIVE", "CNHKG", null );
    }


    // Empty

    @Test
    public void deviation_2a_Empty_CompletionTimeString() throws Exception
    {
        thrown.expect( ConstraintViolationException.class, "NotEmpty()(param1)\", for value ' '" );
        handlingEventParser.parse( " ", trackingIdString, "RECEIVE", "CNHKG", null );
    }

    @Test
    public void step_2_Empty_VoyageNumberString() throws Exception
    {
        // Empty voyage number string is ok
        handlingEventParser.parse( completionTime, trackingIdString, "RECEIVE", "CNHKG", " " );
    }


    // Basic type conversion

    @Test
    public void deviation_3a_TypeConversion_CompletionTimeString() throws Exception
    {
        thrown.expect( InvalidHandlingEventDataException.class,
                       "Invalid date format: '5/27/2011' must be on ISO 8601 format yyyy-MM-dd HH:mm" );
        handlingEventParser.parse( "5/27/2011", trackingIdString, "RECEIVE", "CNHKG", null );
    }

    @Test
    public void deviation_3a_TypeConversion_HandlingEventTypeString() throws Exception
    {
        thrown.expect( InvalidHandlingEventDataException.class, "No enum const class" );
        handlingEventParser.parse( completionTime, trackingIdString, "HAND_OVER", "CNHKG", null );
    }


    // Successful parsing

    @Test
    public void success_Parsing() throws Exception
    {
        handlingEventParser.parse( completionTime, trackingIdString, "RECEIVE", "CNHKG", null );
    }
}