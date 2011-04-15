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

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.samples.dddsample.domain.model.cargo.Leg;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovement;
import org.qi4j.samples.dddsample.domain.model.location.Location;

/**
 * @author edward.yakop@gmail.com
 */
@Mixins( LegEntity.LegMixin.class )
interface LegEntity
    extends Leg, EntityComposite
{
    public class LegMixin
        implements Leg
    {
        @This
        private LegState state;

        public Location from()
        {
            return state.from().get();
        }

        public Location to()
        {
            return state.to().get();
        }

        public CarrierMovement carrierMovement()
        {
            return state.carrierMovement().get();
        }

        public boolean sameValueAs( Leg other )
        {
            return other != null &&
                   carrierMovement().equals( other.carrierMovement() ) &&
                   from().equals( other.from() ) &&
                   to().equals( other.to() );
        }
    }
}
