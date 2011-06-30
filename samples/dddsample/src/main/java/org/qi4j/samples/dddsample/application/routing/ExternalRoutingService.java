package org.qi4j.samples.dddsample.application.routing;

import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.samples.dddsample.domain.model.cargo.Itinerary;
import org.qi4j.samples.dddsample.domain.model.cargo.Leg;
import org.qi4j.samples.dddsample.domain.model.cargo.RouteSpecification;
import org.qi4j.samples.dddsample.domain.model.cargo.assembly.LegState;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovementId;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovementRepository;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;
import org.qi4j.samples.dddsample.domain.service.Routing;
import se.citerus.routingteam.GraphTraversalService;
import se.citerus.routingteam.TransitEdge;
import se.citerus.routingteam.TransitPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Our end of the routing service. This is basically a data model
 * translation layer between our domain model and the API put forward
 * by the routing team, which operates in a different context from us.
 */
@Mixins( ExternalRoutingService.ExternalRoutingServiceMixin.class )
public interface ExternalRoutingService
    extends Routing, ServiceComposite
{
    public class ExternalRoutingServiceMixin
        implements Routing
    {
        @Structure
        TransientBuilderFactory cbf;
        @Structure
        UnitOfWorkFactory uowf;
        @Structure
        QueryBuilderFactory qbf;
        @Service
        private GraphTraversalService graphTraversalService;
        @Service
        private LocationRepository locationRepository;
        @Service
        private CarrierMovementRepository carrierMovementRepository;

        //    @Transactional

        public Query<Itinerary> fetchRoutesForSpecification( RouteSpecification routeSpecification )
        {
            final Location origin = routeSpecification.origin();
            final Location destination = routeSpecification.destination();

            final List<TransitPath> transitPaths = graphTraversalService.findShortestPath(
                origin.unLocode().idString(),
                destination.unLocode().idString()
            );

            final List<Itinerary> itineraries = new ArrayList<Itinerary>();

            for( TransitPath transitPath : transitPaths )
            {
                final Itinerary itinerary = toItinerary( transitPath );
                // Use the specification to safe-guard against invalid itineraries
                //            if( routeSpecification.isSatisfiedBy( itinerary ) )
                {
                    itineraries.add( itinerary );
                }
            }

            return qbf.newQueryBuilder( Itinerary.class ).newQuery( itineraries );
        }

        private Itinerary toItinerary( TransitPath transitPath )
        {
            TransientBuilder<Itinerary> builder = cbf.newTransientBuilder( Itinerary.class );
            Itinerary prototype = builder.prototypeFor( Itinerary.class );
            for( TransitEdge edge : transitPath.getTransitEdges() )
            {
                Leg leg = toLeg( edge );
                prototype.legs().add( prototype.legs().size(), leg );
            }
            return builder.newInstance();
        }

        private Leg toLeg( TransitEdge edge )
        {
            TransientBuilder<Leg> builder = cbf.newTransientBuilder( Leg.class );
            LegState prototype = builder.prototypeFor( LegState.class );
            prototype.carrierMovement()
                .set( carrierMovementRepository.find( new CarrierMovementId( edge.getCarrierMovementId() ) ) );
            UnLocode fromCode = new UnLocode( edge.getFromUnLocode() );
            prototype.from().set( locationRepository.find( fromCode ) );
            UnLocode toCode = new UnLocode( edge.getToUnLocode() );
            prototype.to().set( locationRepository.find( toCode ) );
            return builder.newInstance();
        }
    }
}