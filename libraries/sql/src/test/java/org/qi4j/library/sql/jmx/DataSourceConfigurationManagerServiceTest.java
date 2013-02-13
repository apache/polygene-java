/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.sql.jmx;

import java.beans.PropertyVetoException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.qualifier.IdentifiedBy;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.io.Outputs;
import org.qi4j.io.Receiver;
import org.qi4j.library.circuitbreaker.CircuitBreaker;
import org.qi4j.library.jmx.JMXAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.assembly.DataSourceJMXAssembler;
import org.qi4j.library.sql.c3p0.C3P0DataSourceServiceAssembler;
import org.qi4j.library.sql.common.Databases;
import org.qi4j.library.sql.datasource.DataSources;
import org.qi4j.library.sql.liquibase.LiquibaseConfiguration;
import org.qi4j.library.sql.liquibase.LiquibaseService;
import org.qi4j.test.EntityTestAssembler;

/**
 * Test of export of DataSources to JMX, and some other stuff
 */
public class DataSourceConfigurationManagerServiceTest
{

    public static void main( String[] args )
        throws ActivationException, AssemblyException
    {

        DataSourceConfigurationManagerServiceTest instance = new DataSourceConfigurationManagerServiceTest();
        instance.testDataSources();

        // Hang so it becomes possible to connect through VisualVM and check the JMX beans
        synchronized( instance ) {
            try {
                instance.wait();
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testDataSources()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                new JMXAssembler().assemble( module );

                // Create in-memory store for configurations
                new EntityTestAssembler( Visibility.layer ).assemble( module );

                // Set up DataSource service that will manage the connection pools
                new C3P0DataSourceServiceAssembler().identifiedBy( "datasource-service" ).visibleIn( Visibility.layer ).assemble( module );

                {
                    ModuleAssembly testModule = module.layer().module( "TestDS" );

                    // Create a specific DataSource that uses the "datasource" service to do the main work
                    new DataSourceAssembler().
                            withDataSourceServiceIdentity( "datasource-service" ).
                            identifiedBy( "testds" ).
                            visibleIn( Visibility.module ).
                            withCircuitBreaker( DataSources.newDataSourceCircuitBreaker() ).
                            assemble( testModule );

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
                    new DataSourceAssembler().
                            withDataSourceServiceIdentity( "datasource-service" ).
                            identifiedBy( "testds2" ).
                            visibleIn( Visibility.module ).
                            withCircuitBreaker( DataSources.newDataSourceCircuitBreaker() ).
                            assemble( testModule2 );

                    // Set up Liquibase service that will create the tables
                    testModule2.services( LiquibaseService.class ).identifiedBy( "liquibase2" ).instantiateOnStartup();
                    testModule2.entities( LiquibaseConfiguration.class );
                    testModule2.forMixin( LiquibaseConfiguration.class ).declareDefaults().enabled().set( true );
                    testModule2.forMixin( LiquibaseConfiguration.class ).declareDefaults().changeLog().set( "changelog.xml" );
                }

                // START SNIPPET: jmx
                new DataSourceJMXAssembler().visibleIn( Visibility.module ).assemble( module );
                // END SNIPPET: jmx

            }

        };

//        assembler.application().findModule( "Layer 1","Test" ).objectBuilderFactory().newObjectBuilder( DataSourceConfigurationManagerServiceTest.class ).injectTo( this );
    }

    public void init( @Service @IdentifiedBy( "testds" ) DataSource dataSource, @Service @IdentifiedBy( "testds2" ) ServiceReference<DataSource> dataSource2 )
            throws SQLException, PropertyVetoException
    {
        Databases databases = new Databases( dataSource );

        // Insert some data and print it out
        databases.update( "insert into test values ('id1','foo')" );
        databases.query( "select * from test" ).transferTo( Outputs.withReceiver( new Receiver<ResultSet, SQLException>()
        {
            @Override
            public void receive( ResultSet item )
                    throws SQLException
            {
                System.out.println( item.getString( "id" ) );
            }

        } ) );

        Databases databases2 = new Databases( dataSource2.get() );

        // Insert some data and print it out
        databases2.update( "insert into test values ('id2','bar')" );
        databases2.query( "select * from test" ).transferTo( Outputs.withReceiver( new Receiver<ResultSet, SQLException>()
        {
            @Override
            public void receive( ResultSet item )
                    throws SQLException
            {
                System.out.println( item.getString( "id" ) );
            }

        } ) );

        // Trip the CB
        dataSource2.metaInfo( CircuitBreaker.class ).trip();

        // This should now fail
        try {
            databases2.query( "select * from test" ).transferTo( Outputs.withReceiver( new Receiver<ResultSet, SQLException>()
            {
                @Override
                public void receive( ResultSet item )
                        throws SQLException
                {
                    System.out.println( item.getString( "id" ) );
                }

            } ) );

            Assert.fail();

        } catch ( Throwable e ) {
            // Correct
        }

        // Turn the CB back on
        dataSource2.metaInfo( CircuitBreaker.class ).turnOn();

        // This should now work
        databases2.query( "select * from test" ).transferTo( Outputs.withReceiver( new Receiver<ResultSet, SQLException>()
        {
            @Override
            public void receive( ResultSet item )
                    throws SQLException
            {
                System.out.println( item.getString( "id" ) );
            }

        } ) );

    }

}
