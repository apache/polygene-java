package pathfinder.api;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public final class TransitPath implements Serializable
{

    private final List<TransitEdge> transitEdges;

    /**
     * Constructor.
     *
     * @param transitEdges The legs for this itinerary.
     */
    public TransitPath( final List<TransitEdge> transitEdges )
    {
        this.transitEdges = transitEdges;
    }

    /**
     * @return An unmodifiable list DTOs.
     */
    public List<TransitEdge> getTransitEdges()
    {
        return Collections.unmodifiableList( transitEdges );
    }


    public String print()
    {
        StringBuilder sb = new StringBuilder( "\nTRANSIT PATH -----------------------------------------------------" );
        for (int i = 0; i < transitEdges.size(); i++)
            printLeg( i, sb, transitEdges.get( i ) );
        return sb.append( "\n---------------------------------------------------------------\n" ).toString();
    }
    private void printLeg( int i, StringBuilder sb, TransitEdge edge )
    {
        sb.append( "\n  Leg " ).append( i );
        sb.append( "  Load " );
        sb.append( new SimpleDateFormat( "yyyy-MM-dd" ).format( edge.getFromDate() ) );
        sb.append( " " ).append( edge.getFromUnLocode() );
        sb.append( "   " ).append( edge.getVoyageNumber() );
        sb.append( "   Unload " );
        sb.append( new SimpleDateFormat( "yyyy-MM-dd" ).format( edge.getToDate() ) );
        sb.append( " " ).append( edge.getToUnLocode() );
    }
}
