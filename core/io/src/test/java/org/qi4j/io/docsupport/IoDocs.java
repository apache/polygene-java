package org.qi4j.io.docsupport;

import java.io.File;
import java.io.IOException;
import org.qi4j.io.Inputs;
import org.qi4j.io.Outputs;

// START SNIPPET: io2
import org.qi4j.io.Transforms.Counter;
import static org.qi4j.io.Transforms.map;
// END SNIPPET: io2

public class IoDocs
{
    public static void main( String[] args )
        throws IOException
    {
        {
// START SNIPPET: io1
            File source = new File( "source.txt" );
            File destination = new File( "destination.txt" );
            Inputs.text( source ).transferTo( Outputs.text( destination ) );
// END SNIPPET: io1
        }
        {
// START SNIPPET: io2
            File source = new File( "source.txt" );
            File destination = new File( "destination.txt" );
            Counter<String> counter = new Counter<String>();
            Inputs.text( source ).transferTo( map(counter, Outputs.text(destination) ));
            System.out.println( "Lines: " + counter.count() );
// END SNIPPET: io2
        }
    }
}
