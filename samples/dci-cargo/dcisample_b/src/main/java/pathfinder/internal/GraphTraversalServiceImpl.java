package pathfinder.internal;

import pathfinder.api.GraphTraversalService;
import pathfinder.api.TransitEdge;
import pathfinder.api.TransitPath;

import java.rmi.RemoteException;
import java.util.*;

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
                                               final String destinationUnLocode )
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

                for (TransitPath voyage : voyages)
                {
                    if (depth >= voyage.getTransitEdges().size())
                        continue;

                    final TransitEdge voyageEdge = voyage.getTransitEdges().get( depth );

                    final String departure = voyageEdge.getFromUnLocode();
                    final String arrival = voyageEdge.getToUnLocode();
                    final Date departureTime = voyageEdge.getFromDate();
                    final Date arrivalTime = voyageEdge.getToDate();


                    boolean expectsDeparture = departure.equals( expectedDeparture );
                    boolean uniqueDeparture = !oldDepartures.contains( departure );
                    boolean uniqueArrival = !oldDepartures.contains( arrival );
                    boolean afterLastArrivalTime = departureTime.after( lastArrivalTime );

                    if (expectsDeparture && uniqueDeparture && uniqueArrival && afterLastArrivalTime)
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
            while (!expectedDeparture.equals( destinationUnLocode ) && depth++ < 10);

            // Satisfying routes with at least 2 legs (nextDeparture is the last arrival location)
            if (expectedDeparture.equals( destinationUnLocode ) && routeEdges.size() > 1)
                routeCandidates.add( new TransitPath( routeEdges ) );
        }
        while (routeCandidates.size() < candidateCount && tries++ < maxTries);

        return routeCandidates;
    }

    private List<TransitPath> getShuffledVoyages( List<TransitPath> voyages )
    {
        Collections.shuffle( voyages );
        return voyages;
    }

    public List<TransitPath> getVoyages() throws RemoteException
    {
        return dao.voyages();
    }

    private int getRandomNumberOfCandidates()
    {
        return 3 + random.nextInt( 3 );
    }
}
