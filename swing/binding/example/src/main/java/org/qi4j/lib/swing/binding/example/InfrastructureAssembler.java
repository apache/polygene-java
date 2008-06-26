package org.qi4j.lib.swing.binding.example;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.index.rdf.memory.MemoryRepositoryService;
import org.qi4j.entity.memory.IndexedMemoryEntityStoreService;
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
            IndexedMemoryEntityStoreService.class
        ).visibleIn( application ).instantiateOnStartup();
    }
}
