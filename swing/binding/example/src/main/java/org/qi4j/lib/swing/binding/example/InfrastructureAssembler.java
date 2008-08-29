package org.qi4j.lib.swing.binding.example;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import static org.qi4j.structure.Visibility.application;

/**
 * @author Lan Boon Ping
 */
public class InfrastructureAssembler implements Assembler
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices(
            UuidIdentityGeneratorService.class,
            MemoryRepositoryService.class,
            MemoryEntityStoreService.class
        ).visibleIn( application ).instantiateOnStartup();
    }
}
