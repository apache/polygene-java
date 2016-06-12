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
package org.apache.zest.sample.dcicargo.sample_a.data.shipping.itinerary;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.library.constraints.annotation.NotEmpty;

import static java.time.ZoneOffset.UTC;

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

    LocalDate finalArrivalDate();

    long days();

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

        public LocalDate finalArrivalDate()
        {
            if( lastLeg() == null )
            {
                return LocalDate.MAX;
            }

            return lastLeg().unloadDate().get();
        }

        public long days()
        {
            LocalDate dep = firstLeg().loadDate().get();
            LocalDate arr = lastLeg().unloadDate().get();
            return Duration.between(dep, arr).toDays();
        }
    }
}