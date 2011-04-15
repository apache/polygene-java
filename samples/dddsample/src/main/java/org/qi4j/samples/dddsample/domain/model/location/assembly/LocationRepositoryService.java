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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;

/**
 * @author edward.yakop@gmail.com
 */
@Mixins( LocationRepositoryService.LocationRepositoryMixin.class )
public interface LocationRepositoryService
    extends LocationRepository, ServiceComposite
{
    public class LocationRepositoryMixin
        implements LocationRepository
    {
        private static final String UNKNOWN_UNLOCODE = "XXXXX";
        private static final String UNKNOWN_NAME = "Unknown location";

        @Structure
        private UnitOfWorkFactory uowf;
        @Structure
        private QueryBuilderFactory qbf;
        @Service
        private LocationFactoryService factory;

        public Location unknownLocation()
        {
            UnitOfWork uow = uowf.currentUnitOfWork();

            try
            {
                return uow.get( Location.class, UNKNOWN_UNLOCODE );
            }
            catch( NoSuchEntityException e )
            {
                UnLocode unLocode = new UnLocode( UNKNOWN_UNLOCODE );
                return factory.createLocation( unLocode, UNKNOWN_NAME );
            }
        }

        public Location find( UnLocode unLocode )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            try
            {
                return uow.get( Location.class, unLocode.idString() );
            }
            catch( NoSuchEntityException e )
            {
                return null;
            }
        }

        public Query<Location> findAll()
        {
            UnitOfWork uow = uowf.currentUnitOfWork();

            QueryBuilder<Location> builder = qbf.newQueryBuilder( Location.class );
            final LocationState template = QueryExpressions.templateFor( LocationState.class );
            builder.where( QueryExpressions.eq( template.name(), "Hong Kong" ) );
            return builder.newQuery( uow );
        }
    }
}
