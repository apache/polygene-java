/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.sample.dcicargo.sample_b.communication.query;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.model.IModel;
import org.apache.zest.api.query.Query;
import org.apache.zest.api.query.QueryBuilder;
import org.apache.zest.sample.dcicargo.sample_b.communication.query.dto.CargoDTO;
import org.apache.zest.sample.dcicargo.sample_b.data.entity.CargoEntity;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.location.Location;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.model.EntityModel;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.model.Queries;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.model.QueryModel;

import static org.apache.zest.api.query.QueryExpressions.orderBy;
import static org.apache.zest.api.query.QueryExpressions.templateFor;

/**
 * Common queries
 *
 * Queries shared by Contexts in different packages.
 *
 * Used by the communication layer only. Can change freely according to presentation needs.
 */
public class CommonQueries extends Queries
{
    public IModel<CargoDTO> cargo( String trackingId )
    {
        return EntityModel.of( CargoEntity.class, trackingId, CargoDTO.class );
    }

    public IModel<List<CargoDTO>> cargoList()
    {
        return new QueryModel<CargoDTO, CargoEntity>( CargoDTO.class )
        {
            public Query<CargoEntity> getQuery()
            {
                QueryBuilder<CargoEntity> qb = qbf.newQueryBuilder( CargoEntity.class );
                return uowf.currentUnitOfWork().newQuery( qb )
                    .orderBy( orderBy( templateFor( CargoEntity.class ).trackingId().get().id() ) );
            }
        };
    }

    public List<String> unLocodes()
    {
        QueryBuilder<Location> qb = qbf.newQueryBuilder( Location.class );
        Query<Location> locations = uowf.currentUnitOfWork().newQuery( qb )
            .orderBy( orderBy( templateFor( Location.class ).unLocode().get().code() ) );
        List<String> unLocodeList = new ArrayList<String>();
        for( Location location : locations )
        {
            unLocodeList.add( location.getCode() );
        }

        return unLocodeList;
    }
}