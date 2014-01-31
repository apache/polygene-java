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
package org.qi4j.sample.dcicargo.sample_b.context.service.routing;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.joda.time.LocalDate;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.sample.dcicargo.pathfinder_b.api.GraphTraversalService;
import org.qi4j.sample.dcicargo.pathfinder_b.api.TransitEdge;
import org.qi4j.sample.dcicargo.pathfinder_b.api.TransitPath;
import org.qi4j.sample.dcicargo.sample_b.context.service.routing.exception.FoundNoRoutesException;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Leg;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.Voyage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routing service.
 *
 * This is basically a data model translation layer between our domain model and the
 * API put forward by the path finder team, which operates in a different context from us.
 */
@Mixins( RoutingService.Mixin.class )
public interface RoutingService
    extends ServiceComposite
{
    /**
     * @param routeSpecification route specification
     *
     * @return A list of itineraries that satisfy the specification. May be an empty list if no route is found.
     */
    List<Itinerary> fetchRoutesForSpecification( RouteSpecification routeSpecification )
        throws FoundNoRoutesException;

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

        public List<Itinerary> fetchRoutesForSpecification( RouteSpecification routeSpecification )
            throws FoundNoRoutesException
        {
            final Date departureDate = routeSpecification.earliestDeparture().get();
            final Location origin = routeSpecification.origin().get();
            final Location destination = routeSpecification.destination().get();

            List<TransitPath> transitPaths;
            List<Itinerary> itineraries = new ArrayList<Itinerary>();

            try
            {
                transitPaths = graphTraversalService.findShortestPath( departureDate, origin.getCode(), destination.getCode() );
            }
            catch( RemoteException e )
            {
                logger.error( e.getMessage(), e );
                return Collections.emptyList();
            }

            // The returned result is then translated back into our domain model.
            for( TransitPath transitPath : transitPaths )
            {
                final Itinerary itinerary = toItinerary( transitPath );

                // Use the specification to safe-guard against invalid itineraries
                // We can use the side-effects free method of the RouteSpecification data object
                if( routeSpecification.isSatisfiedBy( itinerary ) )
                {
                    itineraries.add( itinerary );
                }
            }

            if( itineraries.size() == 0 )
            {
                throw new FoundNoRoutesException( destination.name().get(),
                                                  new LocalDate( routeSpecification.arrivalDeadline().get() ) );
            }

            return itineraries;
        }

        private Itinerary toItinerary( TransitPath transitPath )
        {
            ValueBuilder<Itinerary> itinerary = vbf.newValueBuilder( Itinerary.class );
            List<Leg> legs = new ArrayList<Leg>();
            for( TransitEdge edge : transitPath.getTransitEdges() )
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
