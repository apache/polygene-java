package org.qi4j.entitystore.hazelcast.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.hazelcast.HazelcastConfiguration;
import org.qi4j.entitystore.hazelcast.HazelcastEntityStoreService;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

/**
 * @author Paul Merlin <paul@nosphere.org>
 */
public class HazelcastEntityStoreAssembler
    implements Assembler
{
    private Visibility visibility;

    public HazelcastEntityStoreAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( HazelcastEntityStoreService.class ).visibleIn( visibility ).instantiateOnStartup();
        module.services( UuidIdentityGeneratorService.class ).visibleIn( visibility );
        // FIXME Remove from here and update documentation accordingly
        ModuleAssembly config = module.layer().module( "config" );
        config.entities( HazelcastConfiguration.class ).visibleIn( Visibility.layer );
        config.services( MemoryEntityStoreService.class );

    }
}
