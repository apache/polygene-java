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
package org.apache.zest.sample.dcicargo.sample_a.data.shipping.handling;

import java.time.LocalDate;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.cargo.TrackingId;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.location.Location;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.voyage.Voyage;

/**
 * HandlingEvent "collection" - could have had a many-association to
 * Handling Events if it was part of the domain model.
 */
@Mixins( HandlingEvents.Mixin.class )
public interface HandlingEvents
{
    HandlingEvent createHandlingEvent( LocalDate registrationDate,
                                       LocalDate completionDate,
                                       TrackingId trackingId,
                                       HandlingEventType handlingEventType,
                                       Location location,
                                       @Optional Voyage voyage
    )
        throws IllegalArgumentException;

    public abstract class Mixin
        implements HandlingEvents
    {
        @Structure
        UnitOfWorkFactory uowf;

        public HandlingEvent createHandlingEvent( LocalDate registrationDate,
                                                  LocalDate completionDate,
                                                  TrackingId trackingId,
                                                  HandlingEventType handlingEventType,
                                                  Location location,
                                                  Voyage voyage
        )
            throws IllegalArgumentException
        {
            if( voyage == null && handlingEventType.requiresVoyage() )
            {
                throw new IllegalArgumentException( "Voyage is required for handling event type " + handlingEventType );
            }

            else if( voyage != null && handlingEventType.prohibitsVoyage() )
            {
                throw new IllegalArgumentException( "Voyage is not allowed with handling event type " + handlingEventType );
            }

            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<HandlingEvent> handlingEventBuilder = uow.newEntityBuilder( HandlingEvent.class );
            handlingEventBuilder.instance().registrationDate().set( registrationDate );
            handlingEventBuilder.instance().completionDate().set( completionDate );
            handlingEventBuilder.instance().trackingId().set( trackingId );
            handlingEventBuilder.instance().handlingEventType().set( handlingEventType );
            handlingEventBuilder.instance().location().set( location );
            handlingEventBuilder.instance().voyage().set( voyage );  // could be null

            // Save and return
            return handlingEventBuilder.newInstance();
        }
    }
}
