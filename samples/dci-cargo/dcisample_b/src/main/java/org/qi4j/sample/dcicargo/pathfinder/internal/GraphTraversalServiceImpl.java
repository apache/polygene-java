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
package org.qi4j.sample.dcicargo.pathfinder.internal;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.qi4j.sample.dcicargo.pathfinder.api.GraphTraversalService;
import org.qi4j.sample.dcicargo.pathfinder.api.TransitEdge;
import org.qi4j.sample.dcicargo.pathfinder.api.TransitPath;

public class GraphTraversalServiceImpl
    implements GraphTraversalService
{
    private GraphDAO dao;
    private Random random;

    public GraphTraversalServiceImpl( GraphDAO dao )
    {
        this.dao = dao;
        this.random = new Random();
    }

    // Combine existing voyages to create a route.
    public List<TransitPath> findShortestPath( final Date departureDate,
                                               final String originUnLocode,
                                               final String destinationUnLocode
    )
    {
        // Transit paths (itineraries)
        final int candidateCount = getRandomNumberOfCandidates();
        final List<TransitPath> routeCandidates = new ArrayList<TransitPath>( candidateCount );

        int maxTries = 50;
        int tries = 0;
        do
        {
            String expectedDeparture = originUnLocode;
            Date lastArrivalTime = departureDate;

            // Transit edges (itinerary legs)
            final List<TransitEdge> routeEdges = new ArrayList<TransitEdge>();

            // Avoid duplicate locations
            final List<String> oldDepartures = new ArrayList<String>();

            // Loop by depth - enabling chronological order
            int depth = 0;
            do
            {
                final List<TransitPath> voyages = getShuffledVoyages( dao.voyages() );

                // Output for testing...
//                for (int i = 0; i < voyages.size(); i++)
//                    System.out.println( i + " " + voyages.get( i ).print() );

                for( TransitPath voyage : voyages )
                {
                    if( depth >= voyage.getTransitEdges().size() )
                    {
                        continue;
                    }

                    final TransitEdge voyageEdge = voyage.getTransitEdges().get( depth );

                    final String departure = voyageEdge.getFromUnLocode();
                    final String arrival = voyageEdge.getToUnLocode();
                    final Date departureTime = voyageEdge.getFromDate();
                    final Date arrivalTime = voyageEdge.getToDate();

                    boolean expectsDeparture = departure.equals( expectedDeparture );
                    boolean uniqueDeparture = !oldDepartures.contains( departure );
                    boolean uniqueArrival = !oldDepartures.contains( arrival );
                    boolean afterLastArrivalTime = departureTime.after( lastArrivalTime );

                    if( expectsDeparture && uniqueDeparture && uniqueArrival && afterLastArrivalTime )
                    {
                        // Visited departure locations
                        oldDepartures.add( departure );

                        // Go with this carrier movement
                        routeEdges.add( voyageEdge );

                        // Current carrier movement destination will be origin of next movement
                        expectedDeparture = arrival;
                        lastArrivalTime = arrivalTime;

                        // Go deeper to next edge in transit path (later dates)
                        break;
                    }
                }
            }
            while( !expectedDeparture.equals( destinationUnLocode ) && depth++ < 10 );

            // Satisfying routes with at least 2 legs (nextDeparture is the last arrival location)
            if( expectedDeparture.equals( destinationUnLocode ) && routeEdges.size() > 1 )
            {
                routeCandidates.add( new TransitPath( routeEdges ) );
            }
        }
        while( routeCandidates.size() < candidateCount && tries++ < maxTries );

        return routeCandidates;
    }

    private List<TransitPath> getShuffledVoyages( List<TransitPath> voyages )
    {
        Collections.shuffle( voyages );
        return voyages;
    }

    public List<TransitPath> getVoyages()
        throws RemoteException
    {
        return dao.voyages();
    }

    private int getRandomNumberOfCandidates()
    {
        return 3 + random.nextInt( 3 );
    }
}
