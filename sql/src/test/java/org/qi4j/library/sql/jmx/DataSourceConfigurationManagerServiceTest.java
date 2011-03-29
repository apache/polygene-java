package org.qi4j.library.sql.jmx;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.qualifier.IdentifiedBy;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.AssemblySpecifications;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.library.circuitbreaker.CircuitBreaker;
import org.qi4j.library.jmx.JMXAssembler;
import org.qi4j.library.sql.assembler.DataSourceAssembler;
import org.qi4j.library.sql.datasource.DataSourceConfiguration;
import org.qi4j.library.sql.datasource.DataSourceService;
import org.qi4j.library.sql.datasource.Databases;
import org.qi4j.library.sql.liquibase.LiquibaseConfiguration;
import org.qi4j.library.sql.liquibase.LiquibaseService;
import org.qi4j.spi.service.importer.ServiceInstanceImporter;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Test of export of DataSources to JMX, and some other stuff
 */
public class DataSourceConfigurationManagerServiceTest
{
    public static void main( String[] args )
    {

        DataSourceConfigurationManagerServiceTest instance = new DataSourceConfigurationManagerServiceTest();
        instance.testDataSources();

        // Hang so it becomes possible to connect through VisualVM and check the JMX beans
        synchronized(instance)
        {
            try
            {
                instance.wait();
            } catch( InterruptedException e )
            {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testDataSources()
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                new JMXAssembler().assemble( module );

                module.objects( DataSourceConfigurationManagerServiceTest.class );

                // Set up DataSource service that will manage the connection pools
                module.services( DataSourceService.class ).identifiedBy( "datasource" ).visibleIn( Visibility.layer );

                module.entities( DataSourceConfiguration.class ).visibleIn( Visibility.layer );

                {
                    ModuleAssembly testModule = module.layer().module( "TestDS" );

                    // Create a specific DataSource that uses the "datasource" service to do the main work
                    testModule.importedServices( DataSource.class ).
                            importedBy( ServiceInstanceImporter.class ).
                            setMetaInfo( "datasource" ).
                            identifiedBy( "testds" ).
                            visibleIn( Visibility.layer );

                    // Set up Liquibase service that will create the tables
                    testModule.services( LiquibaseService.class ).identifiedBy( "liquibase1" ).instantiateOnStartup();
                    testModule.entities( LiquibaseConfiguration.class );
                    testModule.forMixin( LiquibaseConfiguration.class ).declareDefaults().enabled().set( true );
                    testModule.forMixin( LiquibaseConfiguration.class ).declareDefaults().changeLog().set( "changelog.xml" );
                }

                {
                    ModuleAssembly testModule2 = module.layer().module( "TestDS2" );

                    // Create another specific DataSource that uses the "datasource" service to do the main work
                    // Use DataSourceAssembler to assemble the DataSource.
                    new DataSourceAssembler( "datasource", "testds2" ).assemble( testModule2 );

                    testModule2.importedServices( AssemblySpecifications.types( DataSource.class ) ).visibleIn( Visibility.layer );

                    // Set up Liquibase service that will create the tables
                    testModule2.services( LiquibaseService.class ).identifiedBy( "liquibase2" ).instantiateOnStartup();
                    testModule2.entities( LiquibaseConfiguration.class );
                    testModule2.forMixin( LiquibaseConfiguration.class ).declareDefaults().enabled().set( true );
                    testModule2.forMixin( LiquibaseConfiguration.class ).declareDefaults().changeLog().set( "changelog.xml" );
                }

                // Create in-memory store for configurations
                module.services( MemoryEntityStoreService.class ).visibleIn( Visibility.layer );

                module.services(DataSourceConfigurationManagerService.class).instantiateOnStartup();
            }
        };

        assembler.objectBuilderFactory().newObjectBuilder( DataSourceConfigurationManagerServiceTest.class ).injectTo( this );
    }

    public void init( @Service @IdentifiedBy("testds") DataSource dataSource, @Service @IdentifiedBy("testds2") ServiceReference<DataSource> dataSource2 ) throws SQLException, PropertyVetoException
    {
        Databases databases = new Databases( dataSource );

        // Insert some data and print it out
        databases.update( "insert into test values ('id1','foo')" );
        databases.query( "select * from test" ).transferTo( Outputs.withReceiver( new Receiver<ResultSet, SQLException>()
        {
            @Override
            public void receive( ResultSet item ) throws SQLException
            {
                System.out.println( item.getString( "id" ) );
            }
        } ));

        Databases databases2 = new Databases( dataSource2.get() );

        // Insert some data and print it out
        databases2.update( "insert into test values ('id2','bar')" );
        databases2.query( "select * from test" ).transferTo( Outputs.withReceiver( new Receiver<ResultSet, SQLException>()
        {
            @Override
            public void receive( ResultSet item ) throws SQLException
            {
                System.out.println(item.getString( "id" ));
            }
        }));

        // Trip the CB
        dataSource2.metaInfo( CircuitBreaker.class ).trip();

        // This should now fail
        try
        {
            databases2.query( "select * from test" ).transferTo( Outputs.withReceiver( new Receiver<ResultSet, SQLException>()
            {
                @Override
                public void receive( ResultSet item ) throws SQLException
                {
                    System.out.println(item.getString( "id" ));
                }
            }));

            Assert.fail();

        } catch( Throwable e )
        {
            // Correct
        }

        // Turn the CB back on
        dataSource2.metaInfo( CircuitBreaker.class ).turnOn();

        // This should now work
        databases2.query( "select * from test" ).transferTo( Outputs.withReceiver( new Receiver<ResultSet, SQLException>()
        {
            @Override
            public void receive( ResultSet item ) throws SQLException
            {
                System.out.println(item.getString( "id" ));
            }
        }));

    }
}
