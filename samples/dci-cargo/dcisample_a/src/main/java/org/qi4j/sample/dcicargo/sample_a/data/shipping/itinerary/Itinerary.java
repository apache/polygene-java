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
package org.qi4j.sample.dcicargo.sample_a.data.shipping.itinerary;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

/**
 * An itinerary is a description of a planned route for a cargo.
 *
 * The itinerary has a list of Legs each describing expected
 * loading onto/ unloading from voyages at different locations.
 *
 * List of legs is mandatory and immutable.
 */
@Mixins(Itinerary.Mixin.class)
public interface Itinerary
{
    @NotEmpty
    Property<List<Leg>> legs();

    // Side-effects free and UI agnostic convenience methods
    Leg firstLeg();

    Leg lastLeg();

    ZonedDateTime finalArrivalDate();

    int days();

    public abstract class Mixin
        implements Itinerary
    {
        public Leg firstLeg()
        {
            if( legs().get().isEmpty() )
            {
                return null;
            }

            return legs().get().get( 0 );
        }

        public Leg lastLeg()
        {
            if( legs().get().isEmpty() )
            {
                return null;
            }

            return legs().get().get( legs().get().size() - 1 );
        }

        public ZonedDateTime finalArrivalDate()
        {
            if( lastLeg() == null )
            {
                return ZonedDateTime.from( LocalDate.MAX );
            }

            return lastLeg().unloadTime().get();
        }

        public int days()
        {
            ZonedDateTime dep = firstLeg().loadTime().get();
            ZonedDateTime arr = lastLeg().unloadTime().get();
            return (int) arr.until( dep, ChronoUnit.DAYS );
        }
    }
}