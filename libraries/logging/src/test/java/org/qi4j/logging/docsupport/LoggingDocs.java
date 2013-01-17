package org.qi4j.logging.docsupport;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.logging.debug.Debug;
import org.qi4j.logging.trace.Trace;
import org.qi4j.logging.trace.TraceAllConcern;

public class LoggingDocs
{

// START SNIPPET: logging1
    @Optional @This Debug debug;
// END SNIPPET: logging1

    public LoggingDocs()
    {
// START SNIPPET: logging2
        if( debug != null )
        {
            debug.debug( Debug.NORMAL, "Debugging is made easier." );
        }
// END SNIPPET: logging2
     }

// START SNIPPET: logging3
    public interface ImportantRepository
    {
        @Trace
        void addImportantStuff( ImportantStuff stuff );

        @Trace
        void removeImportantStuff( ImportantStuff stuff );

        ImportantStuff findImportantStuff( String searchKey );
    }
// END SNIPPET: logging3

// START SNIPPET: logging4
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.addServices(ImportantRepository.class)
                .withConcerns( TraceAllConcern.class )
                .withMixins( Debug.class );
    }

// END SNIPPET: logging4

    class ImportantStuff {}
}
