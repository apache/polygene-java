package org.qi4j.samples.cargo.app1.assembly;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.performance.entitystore.memory.MemoryEntityStoreService;

public class ConfigurationModule
    implements Assembler
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( MemoryEntityStoreService.class );
    }
}
