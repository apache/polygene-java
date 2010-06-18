package org.qi4j.entitystore.sql;

import org.qi4j.entitystore.sql.bootstrap.MySQLEntityStoreAssembler;
import org.qi4j.entitystore.sql.database.DatabaseConfiguration;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.test.entity.AbstractEntityStoreTest;

@Ignore // Needs external setup
public class MySQLEntityStoreTest
        extends AbstractEntityStoreTest
{

    @Override
    @SuppressWarnings( "unchecked" )
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        super.assemble( module );
        new MySQLEntityStoreAssembler().assemble( module );
        ModuleAssembly config = module.layerAssembly().moduleAssembly( "config" );
        config.addServices( MemoryEntityStoreService.class );
        config.addEntities( DatabaseConfiguration.class ).visibleIn( Visibility.layer );
    }

    @Test
    @Override
    public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
            throws UnitOfWorkCompletionException
    {
        super.givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification();
    }

    @After
    @Override
    public void tearDown()
            throws Exception
    {
        super.tearDown();
        // TODO : delete test data
    }

}
