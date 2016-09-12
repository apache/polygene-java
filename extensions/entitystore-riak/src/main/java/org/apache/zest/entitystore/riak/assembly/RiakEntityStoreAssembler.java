package org.apache.zest.entitystore.riak.assembly;

import org.apache.zest.bootstrap.Assemblers;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.ServiceDeclaration;
import org.apache.zest.entitystore.riak.RiakEntityStoreConfiguration;
import org.apache.zest.entitystore.riak.RiakMapEntityStoreMixin;
import org.apache.zest.entitystore.riak.RiakMapEntityStoreService;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;

/**
 * Riak EntityStore assembly.
 */
public class RiakEntityStoreAssembler
        extends Assemblers.VisibilityIdentityConfig<RiakEntityStoreAssembler>
{
    @Override
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        module.services( UuidIdentityGeneratorService.class ).
                visibleIn( visibility() );
        ServiceDeclaration service = module.services( RiakMapEntityStoreService.class ).
                withMixins( RiakMapEntityStoreMixin.class ).
                visibleIn( visibility() );
        if( hasIdentity() )
        {
            service.identifiedBy( identity() );
        }
        if( hasConfig() )
        {
            configModule().entities( RiakEntityStoreConfiguration.class ).
                    visibleIn( configVisibility() );
        }
    }
}
