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
package org.qi4j.sample.dcicargo.sample_b.data.structure.cargo;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.qi4j.api.association.Association;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;

/**
 * RouteSpecification
 *
 * Describes:
 * - where a cargo is going from (origin)
 * - the earliest departure date from origin (earliestDeparture)
 * - where a cargo is going to (destination)
 * - when a cargo is expected to arrive (arrivalDeadline)
 *
 * If the route specification needs to change because of unexpected
 * handling events, it's replaced with a new one in {@link Cargo}.
 *
 * An itinerary describes the route a cargo can take, and it satisfies the route
 * specification when the route
 * - starts in the origin location of the route specification origin
 * - starts after the earliest departure of the route specification
 * - ends in the destination location of the route specification
 * - ends before the arrival deadline of the route specification
 *
 * DCI Data is supposed to be dumb. Can we accept to have the specification
 * logic here?
 *
 * All properties are mandatory and immutable.
 */
@Mixins( RouteSpecification.Mixin.class )
public interface RouteSpecification
    extends ValueComposite
{
    Association<Location> origin();

    Association<Location> destination();

    Property<Date> earliestDeparture();

    Property<Date> arrivalDeadline();

    // Side-effects free and UI agnostic convenience methods
    boolean isSatisfiedBy( Itinerary itinerary );

    String print();

    abstract class Mixin
        implements RouteSpecification
    {
        public boolean isSatisfiedBy( Itinerary itinerary )
        {
            return itinerary != null &&
                   !itinerary.legs().get().isEmpty() &&
                   origin().get().equals( itinerary.firstLeg().loadLocation().get() ) &&
                   earliestDeparture().get().before( itinerary.firstLeg().loadTime().get() ) &&
                   destination().get().equals( itinerary.lastLeg().unloadLocation().get() ) &&
                   arrivalDeadline().get().after( itinerary.eta() );
        }

        public String print()
        {
            StringBuilder sb = new StringBuilder(
                "\nROUTE SPECIFICATION ------------" ).
                append( "\n  Origin               " ).append( origin().get() ).
                append( "\n  Destination          " ).append( destination().get() ).
                append( "\n  Earliest departure   " )
                .append( new SimpleDateFormat( "yyyy-MM-dd" ).format( earliestDeparture().get() ) )
                .
                    append( "\n  Arrival deadline     " )
                .append( new SimpleDateFormat( "yyyy-MM-dd" ).format( arrivalDeadline().get() ) )
                .
                    append( "\n--------------------------------" );
            return sb.toString();
        }
    }
}
