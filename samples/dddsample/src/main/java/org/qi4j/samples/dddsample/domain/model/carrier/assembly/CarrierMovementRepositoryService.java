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
package org.qi4j.samples.dddsample.domain.model.carrier.assembly;

import java.util.Date;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovement;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovementId;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovementRepository;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( CarrierMovementRepositoryService.CarrierMovementRepositoryMixin.class )
interface CarrierMovementRepositoryService
    extends CarrierMovementRepository, ServiceComposite
{
    public class CarrierMovementRepositoryMixin
        implements CarrierMovementRepository
    {
        private static final String NONE_CARRIER_MOVEMENT_ID = CarrierMovement.class.getName() + ".NONE";

        @Structure
        private Module module;

        public CarrierMovement noneCarrierMovement()
        {
            UnitOfWork uow = currentUnitOfWork();

            try
            {
                return uow.get( CarrierMovement.class, NONE_CARRIER_MOVEMENT_ID );
            }
            catch( NoSuchEntityException e )
            {
                return createNoneCarrierMovement( uow );
            }
        }

        private UnitOfWork currentUnitOfWork()
        {
            UnitOfWorkFactory uowf = module.unitOfWorkFactory();
            return uowf.currentUnitOfWork();
        }

        private CarrierMovement createNoneCarrierMovement( UnitOfWork uow )
        {
            EntityBuilder<CarrierMovement> builder =
                uow.newEntityBuilder( CarrierMovement.class, NONE_CARRIER_MOVEMENT_ID );
            CarrierMovementState state = builder.instanceFor( CarrierMovementState.class );

            ServiceFinder serviceFinder = module.serviceFinder();
            ServiceReference<LocationRepository> locationRepositoryRef =
                serviceFinder.findService( LocationRepository.class );
            LocationRepository locationRepository = locationRepositoryRef.get();
            Location unknownLocation = locationRepository.unknownLocation();
            state.from().set( unknownLocation );
            state.to().set( unknownLocation );

            Date date = new Date( 0 );
            state.arrivalTime().set( date );
            state.departureTime().set( date );

            return builder.newInstance();
        }

        public CarrierMovement find( CarrierMovementId carrierMovementId )
        {
            UnitOfWork uow = currentUnitOfWork();
            return uow.get( CarrierMovement.class, carrierMovementId.idString() );
        }
    }
}
