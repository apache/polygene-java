package org.qi4j.functional.docsupport;

import java.util.ArrayList;
import java.util.function.Function;

import static org.qi4j.functional.ForEach.forEach;

// START SNIPPET: func2
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

    private static Function<Number, Long> longSum()
    {
        return new Function<Number, Long>()
        {
            long sum;

            @Override
            public Long apply( Number number )
            {
                sum += number.longValue();
                return sum;
            }
        };
    }
}
