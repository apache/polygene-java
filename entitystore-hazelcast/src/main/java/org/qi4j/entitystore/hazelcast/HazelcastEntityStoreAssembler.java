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
    private Visibility visibility;

    public HazelcastEntityStoreAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices( HazelcastEntityStoreService.class ).visibleIn( visibility );
        module.addServices( UuidIdentityGeneratorService.class ).visibleIn( visibility );
    }
}
