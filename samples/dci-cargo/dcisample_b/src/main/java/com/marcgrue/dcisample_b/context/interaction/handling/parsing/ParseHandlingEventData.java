package com.marcgrue.dcisample_b.context.interaction.handling.parsing;

import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import com.marcgrue.dcisample_b.context.interaction.handling.parsing.exception.InvalidHandlingEventDataException;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.library.constraints.annotation.NotEmpty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Parse Handling Event Data  (subfunction use case)
 *
 * First step in the ProcessHandlingEvent use case.
 *
 * Since no Data objects are playing a Role in a Context, it's implemented as a Service
 * instead of a Context. In that respect it doesn't have much to do with DCI, but it shares
 * the intend to implement a use case and we therefore have it in the context package that
 * is then given the broader semantics of the English word "context".
 *
 * Could be implemented as a web service endpoint like HandlingReportServiceImpl,
 * a file upload solution like UploadDirectoryScanner in the DDD sample - or some other
 * technical solution.
 */
@Mixins( ParseHandlingEventData.Mixin.class )
public interface ParseHandlingEventData
      extends ServiceComposite
{
    // Step 1 - Receive handling event data for a handled cargo
    // Step 2 - Verify that data is complete (with annotated constraints)

    public ParsedHandlingEventData parse( @NotEmpty String completionStr,
                                          @NotEmpty String trackingIdStr,
                                          @NotEmpty String handlingEventTypeStr,
                                          @NotEmpty String unLocodeStr,
                                          @Optional String voyageNumberStr
    ) throws InvalidHandlingEventDataException;

    abstract class Mixin
          implements ParseHandlingEventData
    {
        @Structure
        ValueBuilderFactory vbf;

        static final String ISO_8601_FORMAT = "yyyy-MM-dd HH:mm";

        Date completionTime;
        HandlingEventType handlingEventType;


        public ParsedHandlingEventData parse( String completionStr,
                                              String trackingIdStr,
                                              String handlingEventTypeStr,
                                              String unLocodeStr,
                                              String voyageNumberStr
        ) throws InvalidHandlingEventDataException
        {
            // Step 3 - Perform basic type conversion

            try
            {
                completionTime = new SimpleDateFormat( ISO_8601_FORMAT ).parse( completionStr.trim() );
            }
            catch (ParseException e)
            {
                throw new InvalidHandlingEventDataException(
                      "Invalid date format: '" + completionStr + "' must be on ISO 8601 format " + ISO_8601_FORMAT );
            }

            try
            {
                handlingEventType = HandlingEventType.valueOf( handlingEventTypeStr.trim() );
            }
            catch (Exception e)
            {
                throw new InvalidHandlingEventDataException( e.getMessage() );
            }


            // Step 4 - Collect parsed handling event data

            ValueBuilder<ParsedHandlingEventData> parsedData = vbf.newValueBuilder( ParsedHandlingEventData.class );
            parsedData.prototype().registrationTime().set( new Date() );
            parsedData.prototype().completionTime().set( completionTime );
            parsedData.prototype().trackingIdString().set( trackingIdStr );
            parsedData.prototype().handlingEventType().set( handlingEventType );
            parsedData.prototype().unLocodeString().set( unLocodeStr );
            parsedData.prototype().voyageNumberString().set( voyageNumberStr );


            // Step 5 - Return parsed handling event data

            return parsedData.newInstance();
        }
    }
}
