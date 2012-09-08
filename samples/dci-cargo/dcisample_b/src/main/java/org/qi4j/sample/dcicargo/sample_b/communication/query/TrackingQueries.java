/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_b.communication.query;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.model.IModel;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.sample.dcicargo.sample_b.communication.query.dto.HandlingEventDTO;
import org.qi4j.sample.dcicargo.sample_b.data.entity.CargoEntity;
import org.qi4j.sample.dcicargo.sample_b.data.entity.HandlingEventEntity;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.Cargo;
import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;
import org.qi4j.sample.dcicargo.sample_b.infrastructure.model.Queries;
import org.qi4j.sample.dcicargo.sample_b.infrastructure.model.QueryModel;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * Tracking queries
 *
 * Used by the communication layer only. Can change freely according to presentation needs.
 */
public class TrackingQueries extends Queries
{
    public List<String> routedCargos()
    {
        Cargo cargoEntity = templateFor( CargoEntity.class );

        QueryBuilder<CargoEntity> qb = qbf.newQueryBuilder( CargoEntity.class )
            .where( isNotNull( cargoEntity.itinerary() ) );
        Query<CargoEntity> cargos = uowf.currentUnitOfWork().newQuery( qb )
            .orderBy( orderBy( cargoEntity.trackingId().get().id() ) );

        List<String> cargoList = new ArrayList<String>();
        for( CargoEntity cargo : cargos )
        {
            cargoList.add( cargo.trackingId().get().id().get() );
        }

        return cargoList;
    }

    public IModel<List<HandlingEventDTO>> events( final String trackingIdString )
    {
        return new QueryModel<HandlingEventDTO, HandlingEventEntity>( HandlingEventDTO.class )
        {
            public Query<HandlingEventEntity> getQuery()
            {
                HandlingEvent eventTemplate = templateFor( HandlingEvent.class );

                QueryBuilder<HandlingEventEntity> qb = qbf.newQueryBuilder( HandlingEventEntity.class )
                    .where( QueryExpressions.eq( eventTemplate.trackingId().get().id(), trackingIdString ) );
                return uowf.currentUnitOfWork().newQuery( qb )
                    .orderBy( orderBy( eventTemplate.completionTime() ) );
            }
        };
    }
}