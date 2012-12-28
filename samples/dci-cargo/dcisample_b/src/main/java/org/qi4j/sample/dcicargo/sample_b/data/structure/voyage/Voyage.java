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
package org.qi4j.sample.dcicargo.sample_b.data.structure.voyage;

import java.text.SimpleDateFormat;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;

/**
 * Voyage
 *
 * A voyage is a ship, train, flight etc carrying a cargo from one location
 * to another. A {@link Schedule} describes the route it takes.
 *
 * A cargo can be loaded onto part of, or the whole voyage.
 *
 * All properties are mandatory and immutable.
 */
@Mixins( Voyage.Mixin.class )
public interface Voyage
{
    Property<VoyageNumber> voyageNumber();

    Property<Schedule> schedule();

    // Side-effects free and UI agnostic convenience methods
    CarrierMovement carrierMovementDepartingFrom( Location departure );

    String print();

    public abstract class Mixin
        implements Voyage
    {
        public CarrierMovement carrierMovementDepartingFrom( Location departure )
        {
            for( CarrierMovement carrierMovement : schedule().get().carrierMovements().get() )
            {
                if( carrierMovement.departureLocation().get().equals( departure ) )
                {
                    return carrierMovement;
                }
            }

            return null;
        }

        public String print()
        {
            StringBuilder sb = new StringBuilder( "\nVOYAGE " )
                .append( voyageNumber().get().number().get() )
                .append( " -----------------------------------------------------" );

            for( int i = 0; i < schedule().get().carrierMovements().get().size(); i++ )
            {
                printLeg( i, sb, schedule().get().carrierMovements().get().get( i ) );
            }

            return sb.append( "\n---------------------------------------------------------------\n" ).toString();
        }

        private void printLeg( int i, StringBuilder sb, CarrierMovement carrierMovement )
        {
            sb.append( "\n  (Leg " ).append( i ).append( ")" );
            sb.append( "  Departure " );
            sb.append( new SimpleDateFormat( "yyyy-MM-dd" ).format( carrierMovement.departureTime().get() ) );
            sb.append( " " ).append( carrierMovement.departureLocation().get() );
            sb.append( "   Arrival  " );
            sb.append( new SimpleDateFormat( "yyyy-MM-dd" ).format( carrierMovement.arrivalTime().get() ) );
            sb.append( " " ).append( carrierMovement.arrivalLocation().get() );
        }
    }
}
