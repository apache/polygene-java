package com.marcgrue.dcisample_a.communication.query;

import com.marcgrue.dcisample_a.context.support.FoundNoRoutesException;
import com.marcgrue.dcisample_a.context.support.RoutingService;
import com.marcgrue.dcisample_a.data.shipping.itinerary.Itinerary;
import com.marcgrue.dcisample_a.data.shipping.cargo.Cargo;
import com.marcgrue.dcisample_a.infrastructure.model.JSONModel;
import org.apache.wicket.model.IModel;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Booking queries
 *
 * This is in a Qi4j composite since we can then conveniently get the routing service injected.
 * We could choose to implement all query classes like this too.
 *
 * Used by the communication layer only. Can change according to ui needs.
 */
@Mixins( BookingQueries.Mixin.class )
public interface BookingQueries
      extends TransientComposite
{
    List<IModel<Itinerary>> routeCandidates( String trackingIdString ) throws FoundNoRoutesException;

    abstract class Mixin
          implements BookingQueries
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Service
        RoutingService routingService;

        public List<IModel<Itinerary>> routeCandidates( final String trackingIdString ) throws FoundNoRoutesException
        {
            Cargo cargo = uowf.currentUnitOfWork().get( Cargo.class, trackingIdString );
            List<Itinerary> routes = routingService.fetchRoutesForSpecification( cargo.routeSpecification().get() );

            List<IModel<Itinerary>> modelList = new ArrayList<IModel<Itinerary>>();
            for (Itinerary itinerary : routes)
                modelList.add( JSONModel.of( itinerary ) );

            return modelList;
        }
    }
}
