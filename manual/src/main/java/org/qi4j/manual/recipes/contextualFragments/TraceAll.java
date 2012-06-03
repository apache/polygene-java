package org.qi4j.manual.recipes.contextualFragments;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.logging.trace.TraceAllConcern;

// START SNIPPET: assemble
public class TraceAll
{
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        ServiceDeclaration decl = module.addServices( PinSearchService.class );
        if( Boolean.getBoolean( "trace.all"  ) )
        {
            decl.withConcerns( TraceAllConcern.class );
        }
    }
}

// END SNIPPET: assemble
class PinSearchService {}
