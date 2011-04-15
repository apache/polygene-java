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
package org.qi4j.samples.dddsample.domain.model.handling.assembly;

import java.util.Date;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.CargoRepository;
import org.qi4j.samples.dddsample.domain.model.cargo.TrackingId;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovement;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovementId;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovementRepository;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEvent;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEventFactory;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;
import org.qi4j.samples.dddsample.domain.service.UnknownCarrierMovementIdException;
import org.qi4j.samples.dddsample.domain.service.UnknownLocationException;
import org.qi4j.samples.dddsample.domain.service.UnknownTrackingIdException;

/**
 * @author edward.yakop@gmail.com
 */
@Mixins( HandlingEventFactoryService.HandlingEventFactoryMixin.class )
interface HandlingEventFactoryService
    extends HandlingEventFactory, ServiceComposite
{
    public class HandlingEventFactoryMixin
        implements HandlingEventFactory
    {
        @Service
        private CargoRepository cargoRepository;
        @Service
        private CarrierMovementRepository carrierMovementRepository;
        @Service
        private LocationRepository locationRepository;
        @Structure
        private UnitOfWorkFactory uowf;

        public HandlingEvent createHandlingEvent(
            Date completionTime,
            TrackingId trackingId,
            CarrierMovementId carrierMovementId,
            UnLocode unlocode,
            HandlingEvent.Type type
        )
            throws UnknownTrackingIdException, UnknownCarrierMovementIdException, UnknownLocationException
        {
            Cargo cargo = validateCargoExists( trackingId );
            CarrierMovement carrierMovement = findCarrierMovement( carrierMovementId );
            Location location = findLocation( unlocode );
            Date registrationTime = new Date();

            // Create entity
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<HandlingEvent> builder = uow.newEntityBuilder( HandlingEvent.class );
            HandlingEventState state = builder.instanceFor( HandlingEventState.class );
            state.cargo().set( cargo );

            // TODO: remove next line when query + association is implemented
            state.cargoTrackingId().set( trackingId.idString() );

            state.carrierMovement().set( carrierMovement );
            state.location().set( location );
            state.registrationTime().set( registrationTime );
            state.completionTime().set( completionTime );
            state.eventType().set( type );

            return builder.newInstance();
        }

        private Cargo validateCargoExists( TrackingId trackingId )
            throws UnknownTrackingIdException
        {
            Cargo cargo = cargoRepository.find( trackingId );
            if( cargo == null )
            {
                throw new UnknownTrackingIdException( trackingId );
            }

            return cargo;
        }

        private CarrierMovement findCarrierMovement( CarrierMovementId carrierMovementId )
            throws UnknownCarrierMovementIdException
        {
            if( carrierMovementId == null )
            {
                return null;
            }

            CarrierMovement carrierMovement = carrierMovementRepository.find( carrierMovementId );
            if( carrierMovement == null )
            {
                throw new UnknownCarrierMovementIdException( carrierMovementId );
            }

            return carrierMovement;
        }

        private Location findLocation( UnLocode unlocode )
            throws UnknownLocationException
        {
            if( unlocode == null )
            {
                return null;
            }

            Location location = locationRepository.find( unlocode );
            if( location == null )
            {
                throw new UnknownLocationException( unlocode );
            }

            return location;
        }
    }
}
