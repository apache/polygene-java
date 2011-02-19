package org.qi4j.samples.cargo.app1.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.values.EntityToValueService;

public class DomainSupportModuleAssembler
    implements Assembler
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices( EntityToValueService.class ).visibleIn( Visibility.layer );
    }
}
