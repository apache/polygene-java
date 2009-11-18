package org.qi4j.entitystore.neo4j.test;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.neo4j.NeoEntityStoreService;
import org.qi4j.test.entity.AbstractEntityStoreTest;

public class SimpleNeoStoreTest
    extends AbstractEntityStoreTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.layerAssembly().applicationAssembly().setName( "SimpleNeoTest" );

        super.assemble( module );
        module.addServices( NeoEntityStoreService.class );
    }
    
    @Override
    public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
    {
    }
}
