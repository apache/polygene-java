package org.qi4j.samples.cargo.app1.assembly;

import com.pathfinder.api.GraphTraversalService;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.samples.cargo.app1.services.routing.RoutingService;

public class RoutingServiceModuleAssembler
    implements Assembler
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices( RoutingService.class ).visibleIn( Visibility.application );
    }
}
