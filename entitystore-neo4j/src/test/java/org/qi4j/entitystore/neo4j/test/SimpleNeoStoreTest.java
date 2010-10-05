package org.qi4j.entitystore.neo4j.test;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.neo4j.NeoConfiguration;
import org.qi4j.entitystore.neo4j.NeoEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.entity.AbstractEntityStoreTest;

public class SimpleNeoStoreTest
    extends AbstractEntityStoreTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.layerAssembly().applicationAssembly().setName( "SimpleNeoTest" );

        super.assemble( module );
        module.addServices( NeoEntityStoreService.class );

        ModuleAssembly configModule = module.layerAssembly().moduleAssembly( "config" );
        configModule.addEntities( NeoConfiguration.class ).visibleIn( Visibility.layer );
        configModule.addServices( MemoryEntityStoreService.class );
        configModule.addServices( UuidIdentityGeneratorService.class );
    }

    @Override
    public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
    {
    }
}
