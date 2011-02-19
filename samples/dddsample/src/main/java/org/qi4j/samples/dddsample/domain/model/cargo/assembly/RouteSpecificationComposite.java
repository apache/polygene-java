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

import java.util.Date;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.samples.dddsample.domain.model.ValueObject;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.Itinerary;
import org.qi4j.samples.dddsample.domain.model.cargo.RouteSpecification;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.shared.Specification;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( RouteSpecificationComposite.RouteSpecificationMixin.class )
interface RouteSpecificationComposite
    extends RouteSpecification, Specification<Itinerary>, ValueObject<RouteSpecification>, TransientComposite
{
    abstract class RouteSpecificationMixin
        implements RouteSpecification, Specification<Itinerary>, ValueObject<RouteSpecification>
    {
        private
        @Uses
        Cargo cargo;
        private
        @Uses
        Date arrivalDeadline;

        public Location origin()
        {
            return cargo.origin();
        }

        public Location destination()
        {
            return cargo.destination();
        }

        public Date arrivalDeadline()
        {
            return arrivalDeadline;
        }

        public boolean isSatisfiedBy( Itinerary itinerary )
        {
            return true;
        }

        public boolean sameValueAs( RouteSpecification other )
        {
            return other != null &&
                   origin().equals( other.origin() ) &&
                   destination().equals( other.destination() ) &&
                   arrivalDeadline().equals( other.arrivalDeadline() );
        }
    }
}