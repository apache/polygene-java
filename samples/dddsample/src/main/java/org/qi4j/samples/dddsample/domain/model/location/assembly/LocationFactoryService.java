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

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( LocationFactoryService.LocationFactoryMixin.class )
public interface LocationFactoryService
    extends ServiceComposite
{
    /**
     * Creates location.
     *
     * @param unLocode     UnLocode string. This argument must not be {@code null}.
     * @param locationName Location name. This argument must not be {@code null}.
     *
     * @return Newly created location.
     */
    Location createLocation( UnLocode unLocode, String locationName );

    abstract class LocationFactoryMixin
        implements LocationFactoryService
    {
        @Structure
        private UnitOfWorkFactory uowf;

        public Location createLocation( UnLocode unLocode, String locationName )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();

            String locationId = unLocode.idString();
            EntityBuilder<Location> locationBuilder = uow.newEntityBuilder( Location.class, locationId );
            LocationState locationState = locationBuilder.instanceFor( LocationState.class );
            locationState.name().set( locationName );

            return locationBuilder.newInstance();
        }
    }
}
