package com.marcgrue.dcisample_a.data.shipping.itinerary;

import com.marcgrue.dcisample_a.data.shipping.itinerary.Leg;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

import java.util.Date;
import java.util.List;

/**
 * An itinerary is a description of a planned route for a cargo.
 *
 * The itinerary has a list of Legs each describing expected
 * loading onto/ unloading from voyages at different locations.
 *
 * List of legs is mandatory and immutable.
 */
@Mixins( Itinerary.Mixin.class )
public interface Itinerary
{
    @NotEmpty
    Property<List<Leg>> legs();

    // Side-effects free and UI agnostic convenience methods
    Leg firstLeg();
    Leg lastLeg();
    Date finalArrivalDate();
    int days();

    public abstract class Mixin
          implements Itinerary
    {
        public Leg firstLeg()
        {
            if (legs().get().isEmpty())
                return null;

            return legs().get().get( 0 );
        }

        public Leg lastLeg()
        {
            if (legs().get().isEmpty())
                return null;

            return legs().get().get( legs().get().size() - 1 );
        }

        public Date finalArrivalDate()
        {
            if (lastLeg() == null)
                return new Date( new Date( Long.MAX_VALUE ).getTime() );

            return new Date( lastLeg().unloadTime().get().getTime() );
        }

        public int days()
        {
            Date dep = firstLeg().loadTime().get();
            Date arr = lastLeg().unloadTime().get();
            return Days.daysBetween( new LocalDate( dep ), new LocalDate( arr ) ).getDays();
        }
    }
}