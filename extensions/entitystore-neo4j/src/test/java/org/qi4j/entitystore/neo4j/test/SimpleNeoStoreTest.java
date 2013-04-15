package org.qi4j.entitystore.neo4j.test;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.neo4j.NeoConfiguration;
import org.qi4j.entitystore.neo4j.NeoEntityStoreService;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

public class SimpleNeoStoreTest
    extends AbstractEntityStoreTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.layer().application().setName( "SimpleNeoTest" );

        super.assemble( module );
        module.services( FileConfigurationService.class );

        module.services( NeoEntityStoreService.class );

        ModuleAssembly configModule = module.layer().module( "config" );
        configModule.entities( NeoConfiguration.class ).visibleIn( Visibility.layer );
        new EntityTestAssembler().assemble( configModule );
        new OrgJsonValueSerializationAssembler().assemble( module );
    }

    @Override
    public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
    {
    }
}
