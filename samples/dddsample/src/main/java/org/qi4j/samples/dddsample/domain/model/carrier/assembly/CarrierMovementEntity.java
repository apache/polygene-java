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

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovement;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovementId;
import org.qi4j.samples.dddsample.domain.model.location.Location;

import java.util.Date;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( CarrierMovementEntity.CarrierMovementMixin.class )
interface CarrierMovementEntity
    extends CarrierMovement, EntityComposite
{
    public class CarrierMovementMixin
        implements CarrierMovement
    {
        private final CarrierMovementId carrierMovementId;
        @This
        private CarrierMovementState state;

        public CarrierMovementMixin( @This Identity identity )
        {
            String idString = identity.identity().get();
            carrierMovementId = new CarrierMovementId( idString );
        }

        public boolean sameIdentityAs( CarrierMovement other )
        {
            return carrierMovementId.sameValueAs( other.carrierMovementId() );
        }

        public CarrierMovementId carrierMovementId()
        {
            return carrierMovementId;
        }

        public Location from()
        {
            return state.from().get();
        }

        public Location to()
        {
            return state.to().get();
        }

        public Date departureTime()
        {
            return state.departureTime().get();
        }

        public Date arrivalTime()
        {
            return state.arrivalTime().get();
        }
    }
}
