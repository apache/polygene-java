package com.marcgrue.dcisample_a.context.support;

import com.marcgrue.dcisample_a.data.shipping.itinerary.Itinerary;
import com.marcgrue.dcisample_a.data.shipping.itinerary.Leg;
import com.marcgrue.dcisample_a.data.shipping.location.Location;
import com.marcgrue.dcisample_a.data.shipping.voyage.Voyage;
import com.marcgrue.dcisample_a.data.shipping.cargo.RouteSpecification;
import com.marcgrue.dcisample_a.data.shipping.voyage.Voyage;
import org.joda.time.LocalDate;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pathfinder.api.GraphTraversalService;
import pathfinder.api.TransitEdge;
import pathfinder.api.TransitPath;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Routing service.
 *
 * Our end of the routing service. This is basically a data model
 * translation layer between our domain model and the API put forward
 * by the path finder team, which operates in a different context from us.
 */
@Mixins( RoutingService.Mixin.class )
public interface RoutingService
      extends ServiceComposite
{
    /**
     * @param routeSpecification route specification
     * @return A list of itineraries that satisfy the specification. May be an empty list if no route is found.
     */
    List<Itinerary> fetchRoutesForSpecification( RouteSpecification routeSpecification ) throws FoundNoRoutesException;

    abstract class Mixin
          implements RoutingService
    {
        private static final Logger logger = LoggerFactory.getLogger( RoutingService.class );

        @Structure
        ValueBuilderFactory vbf;

        @Structure
        UnitOfWorkFactory uowf;

        @Service
        GraphTraversalService graphTraversalService;

        final static int MAX_TRIES = 10;

        public List<Itinerary> fetchRoutesForSpecification( RouteSpecification routeSpecification ) throws FoundNoRoutesException
        {
            final Location origin = routeSpecification.origin().get();
            final Location destination = routeSpecification.destination().get();

            List<TransitPath> transitPaths;
            List<Itinerary> itineraries = new ArrayList<Itinerary>();

            // Try a MAX_TRIES times to avoid empty results too often
            int tries = 0;
            do
            {
                try
                {
                    transitPaths = graphTraversalService.findShortestPath( origin.getCode(), destination.getCode() );
                }
                catch (RemoteException e)
                {
                    logger.error( e.getMessage(), e );
                    return Collections.emptyList();
                }

                // The returned result is then translated back into our domain model.
                for (TransitPath transitPath : transitPaths)
                {
                    final Itinerary itinerary = toItinerary( transitPath );

                    // Use the specification to safe-guard against invalid itineraries
                    // We can use the side-effects free method of the RouteSpecification data object
                    if (routeSpecification.isSatisfiedBy( itinerary ))
                        itineraries.add( itinerary );
                }
            }
            while (tries++ < MAX_TRIES && itineraries.size() == 0);

            if (itineraries.size() == 0)
                throw new FoundNoRoutesException( destination.name().get(),
                                                  new LocalDate( routeSpecification.arrivalDeadline().get() ) );

            return itineraries;
        }

        private Itinerary toItinerary( TransitPath transitPath )
        {
            ValueBuilder<Itinerary> itinerary = vbf.newValueBuilder( Itinerary.class );
            List<Leg> legs = new ArrayList<Leg>();
            for (TransitEdge edge : transitPath.getTransitEdges())
            {
                legs.add( toLeg( edge ) );
            }
            itinerary.prototype().legs().set( legs );

            return itinerary.newInstance();
        }

        private Leg toLeg( TransitEdge edge )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();

            // Build Leg value object
            ValueBuilder<Leg> leg = vbf.newValueBuilder( Leg.class );
            leg.prototype().voyage().set( uow.get( Voyage.class, edge.getVoyageNumber() ) );
            leg.prototype().loadLocation().set( uow.get( Location.class, edge.getFromUnLocode() ) );
            leg.prototype().unloadLocation().set( uow.get( Location.class, edge.getToUnLocode() ) );
            leg.prototype().loadTime().set( edge.getFromDate() );
            leg.prototype().unloadTime().set( edge.getToDate() );

            return leg.newInstance();
        }
    }
}
