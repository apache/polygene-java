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
package org.qi4j.samples.dddsample.domain.model.location.assembly;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( LocationEntity.LocationMixin.class )
public interface LocationEntity
    extends Location, EntityComposite
{
    public class LocationMixin
        implements Location
    {
        @This
        private LocationState locationState;
        private final UnLocode unlocode;

        public LocationMixin( @This Identity identity )
        {
            String unlocodeString = identity.identity().get();
            unlocode = new UnLocode( unlocodeString );
        }

        public UnLocode unLocode()
        {
            return unlocode;
        }

        public String name()
        {
            return locationState.name().get();
        }

        public boolean sameIdentityAs( Location other )
        {
            String thisUnlocodeIdString = unLocode().idString();
            String otherUnlocodeIdString = other.unLocode().idString();

            return thisUnlocodeIdString.equals( otherUnlocodeIdString );
        }
    }
}
