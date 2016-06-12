/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.sample.dcicargo.sample_b.data.structure.handling;

import java.time.LocalDate;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Immutable;
import org.apache.zest.api.property.Property;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.location.Location;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.tracking.TrackingId;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.voyage.Voyage;

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
public interface HandlingEvent extends Identity
{
    Property<TrackingId> trackingId();

    Property<LocalDate> completionDate();

    Property<LocalDate> registrationDate();

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

            return "\nHANDLING EVENT -----------------" +
                   "\n  Cargo       " + trackingId().get().id().get() +
                   "\n  Type        " + handlingEventType().get().name() +
                   "\n  Location    " + location().get().getString() +
                   "\n  Completed   " + completionDate().get() +
                   "\n  Registered  " + registrationDate().get() +
                   "\n  Voyage      " + voyage +
                   "\n--------------------------------\n";
        }
    }
}