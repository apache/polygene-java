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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.CargoRepository;
import org.qi4j.samples.dddsample.domain.model.cargo.TrackingId;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( CargoRepositoryService.CargoRepositoryMixin.class )
public interface CargoRepositoryService
    extends CargoRepository, ServiceComposite
{
    public class CargoRepositoryMixin
        implements CargoRepository
    {
        @Structure
        private UnitOfWorkFactory uowf;
        @Structure
        private QueryBuilderFactory qbf;

        public Cargo find( TrackingId trackingId )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            try
            {
                return uow.get( Cargo.class, trackingId.idString() );
            }
            catch( NoSuchEntityException e )
            {
                // do nothing
            }
            return null;
        }

        public Query<Cargo> findAll()
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            QueryBuilder<Cargo> builder = qbf.newQueryBuilder( Cargo.class );
            return uow.newQuery( builder );
        }
    }
}