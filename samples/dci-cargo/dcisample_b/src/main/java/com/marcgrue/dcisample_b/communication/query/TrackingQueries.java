package com.marcgrue.dcisample_b.communication.query;

import com.marcgrue.dcisample_b.communication.query.dto.HandlingEventDTO;
import com.marcgrue.dcisample_b.data.structure.cargo.Cargo;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;
import com.marcgrue.dcisample_b.data.entity.CargoEntity;
import com.marcgrue.dcisample_b.data.entity.HandlingEventEntity;
import com.marcgrue.dcisample_b.infrastructure.model.Queries;
import com.marcgrue.dcisample_b.infrastructure.model.QueryModel;
import org.apache.wicket.model.IModel;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;

import java.util.ArrayList;
import java.util.List;

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

        Query<CargoEntity> cargos = qbf.newQueryBuilder( CargoEntity.class )
              .where( isNotNull( cargoEntity.itinerary() ) )
              .newQuery( uowf.currentUnitOfWork() )
              .orderBy( orderBy( cargoEntity.trackingId().get().id() ) );

        List<String> cargoList = new ArrayList<String>();
        for (CargoEntity cargo : cargos)
            cargoList.add( cargo.trackingId().get().id().get() );

        return cargoList;
    }

    public IModel<List<HandlingEventDTO>> events( final String trackingIdString )
    {
        return new QueryModel<HandlingEventDTO, HandlingEventEntity>( HandlingEventDTO.class )
        {
            public Query<HandlingEventEntity> getQuery()
            {
                HandlingEvent eventTemplate = templateFor( HandlingEvent.class );

                return qbf.newQueryBuilder( HandlingEventEntity.class )
                      .where( QueryExpressions.eq( eventTemplate.trackingId().get().id(), trackingIdString ) )
                      .newQuery( uowf.currentUnitOfWork() )
                      .orderBy( orderBy( eventTemplate.completionTime() ) );
            }
        };
    }
}