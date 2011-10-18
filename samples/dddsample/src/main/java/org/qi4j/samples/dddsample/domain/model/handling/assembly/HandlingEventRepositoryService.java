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

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.concern.UnitOfWorkConcern;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.CargoRepository;
import org.qi4j.samples.dddsample.domain.model.cargo.TrackingId;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEvent;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEventRepository;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * @author edward.yakop@gmail.com
 */
@Mixins( HandlingEventRepositoryService.HandlingEventRepositoryMixin.class )
@Concerns( UnitOfWorkConcern.class )
public interface HandlingEventRepositoryService
    extends HandlingEventRepository, ServiceComposite
{
    public class HandlingEventRepositoryMixin
        implements HandlingEventRepository
    {
        @Service
        private CargoRepository cargoRepository;
        @Structure
        private UnitOfWorkFactory uowf;
        @Structure
        private QueryBuilderFactory qbf;

        public Query<HandlingEvent> findEventsForCargo( TrackingId trackingId )
        {
            Cargo cargo = cargoRepository.find( trackingId );

            // Create query
            UnitOfWork uow = uowf.currentUnitOfWork();
            QueryBuilder<HandlingEvent> builder = qbf.newQueryBuilder( HandlingEvent.class );
            HandlingEventState eventTemplate = templateFor( HandlingEventState.class );

            // TODO: Uncomment next line when this query with association is supported.
//            builder.where( eq( eventTemplate.cargo(), cargo ) );

            builder.where( eq( eventTemplate.cargoTrackingId(), trackingId.idString() ) );

            Query<HandlingEvent> handlingEventQuery = uow.newQuery( builder );
            handlingEventQuery.orderBy( orderBy( eventTemplate.completionTime() ) );

            return handlingEventQuery;
        }
    }
}
