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
package org.qi4j.sample.dcicargo.sample_a.data.shipping.handling;

import java.util.Date;
import org.qi4j.api.association.Association;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.TrackingId;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.location.Location;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage.Voyage;

/**
 * A HandlingEvent is used to register the event when, for instance,
 * a cargo is unloaded from a carrier at some location at a given time.
 *
 * The HandlingEvent's are sent from different Incident Logging Applications
 * some time after the event occurred and contain information about the
 * {@link TrackingId}, {@link Location}, timestamp of the completion of the event,
 * and possibly, if applicable a {@link Voyage}.
 *
 * HandlingEvent's could contain information about a {@link Voyage} and if so,
 * the event type must be either {@link HandlingEventType#LOAD} or
 * {@link HandlingEventType#UNLOAD}.
 *
 * All other events must be of {@link HandlingEventType#RECEIVE},
 * {@link HandlingEventType#CLAIM} or {@link HandlingEventType#CUSTOMS}.
 * (Handling event type is mandatory).
 */
public interface HandlingEvent
{
    @Immutable
    Property<Date> registrationTime();

    @Immutable
    Property<Date> completionTime();

    @Immutable
    Property<TrackingId> trackingId();

    @Immutable
    Property<HandlingEventType> handlingEventType();

    @Immutable
    Association<Location> location();

    @Optional
    @Immutable
    Association<Voyage> voyage();

    @Optional
    @UseDefaults
    Property<Boolean> wasUnexpected();
}