package com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto;

import com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType;
import com.marcgrue.dcisample_b.infrastructure.conversion.DTO;
import org.qi4j.api.common.Optional;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The ParsedHandlingEventData simply helps move submitted event registration data around.
 */
@Immutable
@Mixins( ParsedHandlingEventData.Mixin.class )
public interface ParsedHandlingEventData extends DTO
{
    Property<Date> registrationTime();

    Property<Date> completionTime();

    Property<String> trackingIdString();

    Property<HandlingEventType> handlingEventType();

    Property<String> unLocodeString();

    @Optional
    Property<String> voyageNumberString();

    public String print();

    abstract class Mixin
          implements ParsedHandlingEventData
    {
        public String print()
        {
            String voyage = "";
            if (voyageNumberString().get() != null)
                voyage = voyageNumberString().get();

            SimpleDateFormat date = new SimpleDateFormat( "yyyy-MM-dd" );

            StringBuilder builder = new StringBuilder( "\nPARSED HANDLING EVENT DATA -----------------" ).
                  append( "\n  Tracking id string           " ).append( trackingIdString().get() ).
                  append( "\n  Handling Event Type string   " ).append( handlingEventType().get().name() ).
                  append( "\n  UnLocode string              " ).append( unLocodeString().get() ).
                  append( "\n  Completed string             " ).append( date.format( completionTime().get() ) ).
                  append( "\n  Registered string            " ).append( date.format( registrationTime().get() ) ).
                  append( "\n  Voyage string                " ).append( voyage ).
                  append( "\n--------------------------------------------\n" );

            return builder.toString();
        }
    }
}
