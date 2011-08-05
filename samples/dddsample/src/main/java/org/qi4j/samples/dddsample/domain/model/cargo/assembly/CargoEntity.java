/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.samples.dddsample.domain.model.cargo.assembly;

import org.qi4j.api.association.Association;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.DeliveryHistory;
import org.qi4j.samples.dddsample.domain.model.cargo.Itinerary;
import org.qi4j.samples.dddsample.domain.model.cargo.TrackingId;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEvent;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;

import static org.qi4j.samples.dddsample.domain.model.handling.HandlingEvent.Type.UNLOAD;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Concerns( ItineraryLifecycleConcern.class )
@Mixins( { CargoEntity.CargoMixin.class, DeliveryHistoryMixin.class } )
interface CargoEntity
    extends Cargo, DeliveryHistory, EntityComposite
{
    public class CargoMixin
        implements Cargo
    {
        @This
        DeliveryHistory history;
        @Structure
        private Module module;

        private final CargoState state;
        private final TrackingId trackingId;

        public CargoMixin(
            @This Identity identity,
            @This CargoState cargoState,
            @Structure TransientBuilderFactory cbf
        )
        {
            state = cargoState;

            String trackingIdString = identity.identity().get();
            trackingId = new TrackingId( trackingIdString );
        }

        public TrackingId trackingId()
        {
            return trackingId;
        }

        public Location origin()
        {
            return state.origin().get();
        }

        public void changeDestination( Location newDestination )
        {
            state.destination().set( newDestination );
        }

        public Location destination()
        {
            return state.destination().get();
        }

        public Itinerary itinerary()
        {
            return state.itinerary().get();
        }

        public Location lastKnownLocation()
        {
            HandlingEvent lastEvent = history.lastEvent();
            if( lastEvent != null )
            {
                return lastEvent.location();
            }
            else
            {
                return unknownLocation();
            }
        }

        private Location unknownLocation()
        {
            ServiceReference<LocationRepository> reference = module.findService( LocationRepository.class );
            LocationRepository locationRepository = reference.get();

            return locationRepository.unknownLocation();
        }

        public boolean hasArrived()
        {
            return destination().equals( lastKnownLocation() );
        }

        public void attachItinerary( Itinerary itinerary )
        {
            detachItinerary();

            state.itinerary().set( itinerary );
            ItineraryEntity itineraryEntity = (ItineraryEntity) itinerary;
            itineraryEntity.changeCargo( this );
        }

        public void detachItinerary()
        {
            Association<Itinerary> itineraryAssociation = state.itinerary();
            ItineraryEntity itineraryEntity = (ItineraryEntity) itineraryAssociation.get();
            itineraryEntity.detachCargo();

            itineraryAssociation.set( null );
        }

        public boolean isMisdirected()
        {
            HandlingEvent lastEvent = history.lastEvent();
            return lastEvent != null && !state.itinerary().get().isExpected( lastEvent );
        }

        public boolean isUnloadedAtDestination()
        {
            Query<HandlingEvent> events = history.eventsOrderedByCompletionTime();
            for( HandlingEvent event : events )
            {
                if( UNLOAD.equals( event.eventType() ) &&
                    destination().equals( event.location() ) )
                {
                    return true;
                }
            }

            return false;
        }

        public boolean sameIdentityAs( Cargo other )
        {
            return other != null && trackingId.sameValueAs( other.trackingId() );
        }
    }
}