package org.qi4j.functional.docsupport;

import java.util.ArrayList;

// START SNIPPET: func2
import static org.qi4j.functional.ForEach.forEach;
import static org.qi4j.functional.Functions.longSum;
// END SNIPPET: func2

public class FunctionalDocs
{
    public static void main( String[] args )
    {
        {
// START SNIPPET: func1
            Iterable<Long> data = new ArrayList<Long>();
// END SNIPPET: func1
// START SNIPPET: func1

            long sum = 0;
            for( Long point : data )
            {
                sum = sum + point;
            }
            System.out.println( "The sum is " + sum );
// END SNIPPET: func1
        }
        {
// START SNIPPET: func2
            Iterable<Number> data = new ArrayList<Number>();
            Long sum = forEach( data ).map( longSum() ).last();
            System.out.println( "The sum is " + sum );

// END SNIPPET: func2
        }
    }
}
