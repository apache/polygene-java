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
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovement;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEvent;
import org.qi4j.samples.dddsample.domain.model.location.Location;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( HandlingEventEntity.HandlingEventMixin.class )
interface HandlingEventEntity
    extends HandlingEvent, EntityComposite
{
    public class HandlingEventMixin
        implements HandlingEvent
    {
        @This
        private HandlingEventState state;
        @Structure
        private Module module;

        public Type eventType()
        {
            return state.eventType().get();
        }

        public CarrierMovement carrierMovement()
        {
            return state.carrierMovement().get();
        }

        public Date completionTime()
        {
            return state.completionTime().get();
        }

        public Date registrationTime()
        {
            return state.registrationTime().get();
        }

        public Location location()
        {
            return state.location().get();
        }

        public Cargo cargo()
        {
            return state.cargo().get();
        }

        public boolean sameEventAs( HandlingEvent other )
        {
            return other != null &&
                   cargo().equals( other.cargo() ) &&
                   carrierMovement().equals( other.carrierMovement() ) &&
                   completionTime().equals( other.completionTime() ) &&
                   location().equals( other.location() ) &&
                   eventType().equals( other.eventType() );
        }
    }
}
