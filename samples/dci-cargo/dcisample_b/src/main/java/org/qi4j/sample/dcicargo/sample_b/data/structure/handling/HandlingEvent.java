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
package org.qi4j.sample.dcicargo.sample_b.data.structure.handling;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.qi4j.api.association.Association;
import org.qi4j.api.common.Optional;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;
import org.qi4j.sample.dcicargo.sample_b.data.structure.tracking.TrackingId;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.Voyage;

/**
 * HandlingEvent
 *
 * A HandlingEvent is used to register the event when, for instance,
 * a cargo is unloaded from a carrier at some location at a given time.
 *
 * The HandlingEvents are sent from different Incident Logging Applications
 * some time after the event occurred and contain information about the
 * {@link TrackingId}, {@link Location}, timestamp of the completion of the event,
 * and possibly, if applicable a {@link Voyage}.
 *
 * Note that we don't save the whole cargo graph here as in the DDD sample! With
 * the tracking id saved only, our HandlingEvent objects become much lighter and
 * faster to save as we register incoming handling event data in high volumes from
 * incident logging applications.
 *
 * HandlingEvents could contain information about a {@link Voyage} and if so,
 * the event type must be either {@link HandlingEventType#LOAD} or
 * {@link HandlingEventType#UNLOAD}.
 *
 * All other events must be of type {@link HandlingEventType#RECEIVE},
 * {@link HandlingEventType#CLAIM} or {@link HandlingEventType#CUSTOMS}.
 */
@Immutable
@Mixins( HandlingEvent.Mixin.class )
public interface HandlingEvent
{
    Property<Date> registrationTime();

    Property<Date> completionTime();

    Property<TrackingId> trackingId();

    Property<HandlingEventType> handlingEventType();

    Association<Location> location();

    @Optional
    Association<Voyage> voyage();

    String print();

    abstract class Mixin
        implements HandlingEvent
    {
        public String print()
        {
            String voyage = "";
            if( voyage().get() != null )
            {
                voyage = voyage().get().voyageNumber().get().number().get();
            }

            SimpleDateFormat date = new SimpleDateFormat( "yyyy-MM-dd" );

            StringBuilder builder = new StringBuilder( "\nHANDLING EVENT -----------------" ).
                append( "\n  Cargo       " ).append( trackingId().get().id().get() ).
                append( "\n  Type        " ).append( handlingEventType().get().name() ).
                append( "\n  Location    " ).append( location().get().getString() ).
                append( "\n  Completed   " ).append( date.format( completionTime().get() ) ).
                append( "\n  Registered  " ).append( date.format( registrationTime().get() ) ).
                append( "\n  Voyage      " ).append( voyage ).
                append( "\n--------------------------------\n" );

            return builder.toString();
        }
    }
}