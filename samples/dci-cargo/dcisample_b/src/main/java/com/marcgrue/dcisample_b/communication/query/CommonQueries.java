package com.marcgrue.dcisample_b.communication.query;

import com.marcgrue.dcisample_b.communication.query.dto.CargoDTO;
import com.marcgrue.dcisample_b.data.entity.CargoEntity;
import com.marcgrue.dcisample_b.data.structure.location.Location;
import com.marcgrue.dcisample_b.infrastructure.model.EntityModel;
import com.marcgrue.dcisample_b.infrastructure.model.Queries;
import com.marcgrue.dcisample_b.infrastructure.model.QueryModel;
import org.apache.wicket.model.IModel;
import org.qi4j.api.query.Query;

import java.util.ArrayList;
import java.util.List;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

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
                return qbf.newQueryBuilder( CargoEntity.class ).newQuery( uowf.currentUnitOfWork() )
                      .orderBy( orderBy( templateFor( CargoEntity.class ).trackingId().get().id() ) );
            }
        };
    }

    public List<String> unLocodes()
    {
        Query<Location> locations = qbf.newQueryBuilder( Location.class ).newQuery( uowf.currentUnitOfWork() )
              .orderBy( orderBy( templateFor( Location.class ).unLocode().get().code() ) );
        List<String> unLocodeList = new ArrayList<String>();
        for (Location location : locations)
            unLocodeList.add( location.getCode() );

        return unLocodeList;
    }
}