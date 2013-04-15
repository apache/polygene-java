package org.qi4j.manual.recipes.io;

import java.io.File;
import java.io.IOException;
import org.qi4j.io.Inputs;
import org.qi4j.io.Outputs;

public class Docs
{

    public void filter()
        throws IOException
    {
// START SNIPPET: filter
        File source = new File("source.txt");
        File destination = new File("destination.txt");
        Inputs.text( source ).transferTo( Outputs.text( destination ) );
// END SNIPPET: filter
    }
}
