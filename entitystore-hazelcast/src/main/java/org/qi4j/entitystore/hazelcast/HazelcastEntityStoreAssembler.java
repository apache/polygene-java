package org.qi4j.entitystore.hazelcast;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

/**
 * @author Paul Merlin <paul@nosphere.org>
 */
public class HazelcastEntityStoreAssembler
        implements Assembler
{

    private final String configModuleName;

    public HazelcastEntityStoreAssembler(String configModuleName)
    {
        this.configModuleName = configModuleName;

    }

    public void assemble(ModuleAssembly module)
            throws AssemblyException
    {
        module.addServices(HazelcastEntityStoreService.class, UuidIdentityGeneratorService.class).visibleIn(Visibility.layer);
        ModuleAssembly config = module.layerAssembly().moduleAssembly(configModuleName);
        config.addEntities(HazelcastConfiguration.class).visibleIn(Visibility.layer);
        config.addServices(MemoryEntityStoreService.class);
    }

}
