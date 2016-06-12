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
package org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.parsing.dto;

import java.time.LocalDate;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Immutable;
import org.apache.zest.api.property.Property;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.conversion.DTO;

/**
 * The ParsedHandlingEventData simply helps move submitted event registration data around.
 */
@Immutable
@Mixins( ParsedHandlingEventData.Mixin.class )
public interface ParsedHandlingEventData
{
    Property<LocalDate> registrationDate();

    Property<LocalDate> completionDate();

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
            if( voyageNumberString().get() != null )
            {
                voyage = voyageNumberString().get();
            }

            return "\nPARSED HANDLING EVENT DATA -----------------" +
                   "\n  Tracking id string           " + trackingIdString().get() +
                   "\n  Handling Event Type string   " + handlingEventType().get().name() +
                   "\n  UnLocode string              " + unLocodeString().get() +
                   "\n  Completed string             " + completionDate().get() +
                   "\n  Registered string            " + registrationDate().get() +
                   "\n  Voyage string                " + voyage +
                   "\n--------------------------------------------\n";
        }
    }
}
