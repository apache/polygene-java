package org.qi4j.entitystore.sql;

import org.qi4j.entitystore.sql.bootstrap.MySQLEntityStoreAssembler;
import org.qi4j.entitystore.sql.database.DatabaseConfiguration;
import java.io.File;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.performance.AbstractEntityStorePerformanceTest;

/**
 * Performance test for MySQLEntityStoreComposite
 */
@Ignore // Too long and needs external setup
public class MySQLEntityStorePerformanceTest
        extends AbstractEntityStorePerformanceTest
{

    public MySQLEntityStorePerformanceTest()
    {
        super( "MySQLEntityStore", createAssembler() );
    }

    private static Assembler createAssembler()
    {
        return new Assembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                new MySQLEntityStoreAssembler( Visibility.application ).assemble( module );
                ModuleAssembly configModule = module.layerAssembly().moduleAssembly( "Config" );
                configModule.addEntities( DatabaseConfiguration.class ).visibleIn( Visibility.layer );
                new EntityTestAssembler( Visibility.module ).assemble( configModule );
            }

        };
    }

    @Test
    @Override
    public void whenCreateEntityWithSinglePropertyThenRecordIterationsPerSecond()
            throws Exception
    {
        super.whenCreateEntityWithSinglePropertyThenRecordIterationsPerSecond();
    }

    @Test
    @Override
    public void whenCreateEntityWithSinglePropertyInBatchThenRecordIterationsPerSecond()
            throws Exception
    {
        super.whenCreateEntityWithSinglePropertyInBatchThenRecordIterationsPerSecond();
    }

    public void ____cleanUp()
            throws Exception
    {
        super.cleanUp();
        File dbFile = new File( "target/jdbmstore.data.db" );
        boolean success = true;
        if ( dbFile.exists() ) {
            success = dbFile.delete();
        }

        File logFile = new File( "qi4j/jdbmstore.data.lg" );
        if ( logFile.exists() ) {
            success = success & logFile.delete();
        }
        if ( !success ) {
            throw new Exception( "Could not delete test data" );
        }
    }

}
