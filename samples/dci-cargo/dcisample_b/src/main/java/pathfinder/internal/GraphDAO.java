package pathfinder.internal;

import pathfinder.api.TransitEdge;
import pathfinder.api.TransitPath;

import java.util.*;

public class GraphDAO
{

    private static final Random random = new Random();
    private static final long ONE_MIN_MS = 1000 * 60;
    private static final long ONE_HOUR_MS = ONE_MIN_MS * 60;
    private static final long ONE_DAY_MS = ONE_HOUR_MS * 24;

    private List<TransitPath> voyages = new ArrayList<TransitPath>();

    public List<String> listLocations()
    {
        return new ArrayList<String>( Arrays.asList(
              "CNHKG", "AUMEL", "SESTO", "FIHEL", "USCHI", "JNTKO", "DEHAM", "CNSHA", "NLRTM", "SEGOT", "CNHGH", "SOMGQ", "USNYC", "USDAL"
        ) );
    }

    public List<TransitPath> voyages()
    {
        if (voyages.size() > 0)
            return voyages;

        Date departureDate = new Date();
        for (int i = 0; i < 50; i++)
        {
            List<String> locations = getRandomChunkOfLocations( listLocations() );
            final List<TransitEdge> transitEdges = new ArrayList<TransitEdge>( locations.size() - 1 );
            final String voyageNumber = "V" + ( 101 + i );

            // Origin and destination of voyage schedule
            String from = locations.remove( 0 );
            String destination = locations.remove( 0 );

            Date date = nextDate( departureDate );
            Date fromDate;
            Date toDate;

            // Carrier movements
            for (final String to : locations)
            {
                fromDate = nextDate( date );
                toDate = nextDate( fromDate );
                date = nextDate( toDate );
                transitEdges.add( new TransitEdge( voyageNumber, from, to, fromDate, toDate ) );

                // Arrival location of last carrier movement becomes departure location of next
                from = to;
            }

            // Final carrier movement
            fromDate = nextDate( date );
            toDate = nextDate( fromDate );
            transitEdges.add( new TransitEdge( voyageNumber, from, destination, fromDate, toDate ) );

            voyages.add( new TransitPath( transitEdges ) );
        }

        // Output for testing...
//        for (int i = 0; i < voyages.size(); i++)
//            System.out.println( i + " " + voyages.get( i ).print() );

        return voyages;
    }

    private Date nextDate( Date date )
    {
        return new Date( date.getTime() + ONE_DAY_MS + ( random.nextInt( 1000 ) - 500 ) * ONE_MIN_MS );
    }

    private List<String> getRandomChunkOfLocations( List<String> allLocations )
    {
        Collections.shuffle( allLocations );
        final int total = allLocations.size();
        // Including origin and destination
        final int chunk = total > 6 ? 3 + new Random().nextInt( 5 ) : total;
        return allLocations.subList( 0, chunk );
    }
}
