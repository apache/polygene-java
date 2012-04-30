package com.marcgrue.dcisample_b.data.structure.itinerary;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.constraints.annotation.NotEmpty;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Itinerary
 *
 * Describes a planned route for a Cargo.
 *
 * The Itinerary has a list of Legs each describing expected loading onto/ unloading from
 * voyages at different locations. The list of legs is mandatory, immutable and can't be empty.
 */
@Mixins( Itinerary.Mixin.class )
public interface Itinerary
      extends ValueComposite
{
    @NotEmpty
    Property<List<Leg>> legs();

    // Side-effects free and UI agnostic convenience methods
    Leg firstLeg();
    Leg leg( Integer current );
    Leg lastLeg();
    Date eta();
    int days();
    String print();

    public abstract class Mixin
          implements Itinerary
    {
        public Leg firstLeg()
        {
            return legs().get().get( 0 );
        }

        public Leg leg( Integer index )
        {
            if (index < 0 || index + 1 > legs().get().size())
                return null;

            return legs().get().get( index );
        }

        public Leg lastLeg()
        {
            return legs().get().get( legs().get().size() - 1 );
        }

        public Date eta()
        {
            return lastLeg().unloadTime().get();
        }
        public int days()
        {
            Date dep = firstLeg().loadTime().get();
            Date arr = lastLeg().unloadTime().get();
            return Days.daysBetween( new LocalDate( dep ), new LocalDate( arr ) ).getDays();
        }

        public String print()
        {
            StringBuilder sb = new StringBuilder( "\nITINERARY -----------------------------------------------------" );
            for (int i = 0; i < legs().get().size(); i++)
                printLeg( i, sb, legs().get().get( i ) );
            return sb.append( "\n---------------------------------------------------------------\n" ).toString();
        }
        private void printLeg( int i, StringBuilder sb, Leg leg )
        {
            sb.append( "\n  Leg " ).append( i );
            sb.append( "  Load " );
            sb.append( new SimpleDateFormat( "yyyy-MM-dd" ).format( leg.loadTime().get() ) );
            sb.append( " " ).append( leg.loadLocation().get() );
            sb.append( "   " ).append( leg.voyage().get() );
            sb.append( "   Unload " );
            sb.append( new SimpleDateFormat( "yyyy-MM-dd" ).format( leg.unloadTime().get() ) );
            sb.append( " " ).append( leg.unloadLocation().get() );
        }
    }
}