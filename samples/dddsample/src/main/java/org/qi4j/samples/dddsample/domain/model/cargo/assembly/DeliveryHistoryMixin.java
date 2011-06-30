/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.samples.dddsample.domain.model.cargo.assembly;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.Query;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.DeliveryHistory;
import org.qi4j.samples.dddsample.domain.model.cargo.StatusCode;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovement;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovementRepository;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEvent;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEventRepository;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;

import java.util.Iterator;

/**
 * JAVADOC
 */
public class DeliveryHistoryMixin
    implements DeliveryHistory
{
    @This
    private Cargo cargo;
    @Service
    private CarrierMovementRepository carrierMovementRepository;
    @Service
    private LocationRepository locationRepository;
    @Service
    private HandlingEventRepository handlingEventRepository;

    public Query<HandlingEvent> eventsOrderedByCompletionTime()
    {
        return handlingEventRepository.findEventsForCargo( cargo.trackingId() );
    }

    public HandlingEvent lastEvent()
    {
        Query<HandlingEvent> events = eventsOrderedByCompletionTime();
        long numberOfEvent = events.count();
        if( numberOfEvent == 0 )
        {
            return null;
        }
        events.firstResult( (int) ( numberOfEvent - 1 ) );
        events.maxResults( 1 );
        final Iterator<HandlingEvent> handlingEventIterator = events.iterator();
        if( handlingEventIterator.hasNext() )
        {
            return handlingEventIterator.next();
        }
        return null;
    }

    public StatusCode status()
    {
        if( lastEvent() == null )
        {
            return org.qi4j.samples.dddsample.domain.model.cargo.StatusCode.NOT_RECEIVED;
        }

        HandlingEvent.Type type = lastEvent().eventType();
        switch( type )
        {
        case LOAD:
            return org.qi4j.samples.dddsample.domain.model.cargo.StatusCode.ONBOARD_CARRIER;

        case UNLOAD:
        case RECEIVE:
        case CUSTOMS:
            return org.qi4j.samples.dddsample.domain.model.cargo.StatusCode.IN_PORT;

        case CLAIM:
            return org.qi4j.samples.dddsample.domain.model.cargo.StatusCode.CLAIMED;

        default:
            return null;
        }
    }

    public Location currentLocation()
    {
        if( status().equals( org.qi4j.samples.dddsample.domain.model.cargo.StatusCode.IN_PORT ) )
        {
            return lastEvent().location();
        }
        else
        {
            return unknownLocation();
        }
    }

    private Location unknownLocation()
    {
        return locationRepository.unknownLocation();
    }

    public CarrierMovement currentCarrierMovement()
    {
        StatusCode statusCode = status();
        if( org.qi4j.samples.dddsample.domain.model.cargo.StatusCode.ONBOARD_CARRIER.equals( statusCode ) )
        {
            return lastEvent().carrierMovement();
        }
        else
        {
            return noneCarrierMovement();
        }
    }

    private CarrierMovement noneCarrierMovement()
    {
        return carrierMovementRepository.noneCarrierMovement();
    }

    public boolean sameValueAs( DeliveryHistory other )
    {
        return other != null && eventsOrderedByCompletionTime().equals( other.eventsOrderedByCompletionTime() );
    }
}
