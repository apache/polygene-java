/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.library.sql.jmx;

import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.apache.polygene.library.jmx.JMXAssembler;
import org.apache.polygene.library.sql.assembly.DataSourceAssembler;
import org.apache.polygene.library.sql.assembly.DataSourceJMXAssembler;
import org.apache.polygene.library.sql.datasource.DataSources;
import org.apache.polygene.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.polygene.library.sql.liquibase.LiquibaseAssembler;
import org.apache.polygene.library.sql.liquibase.LiquibaseConfiguration;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

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
        synchronized( instance )
        {
            try
            {
                instance.wait();
            }
            catch( InterruptedException e )
            {
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
                new EntityTestAssembler().visibleIn( Visibility.layer ).assemble( module );

                // Set up DataSource service that will manage the connection pools
                new DBCPDataSourceServiceAssembler().identifiedBy( "datasource-service" )
                                                    .visibleIn( Visibility.layer )
                                                    .withConfig( module, Visibility.layer )
                                                    .assemble( module );

                {
                    ModuleAssembly testModule = module.layer().module( "TestDS" );

                    // Create a specific DataSource that uses the "datasource" service to do the main work
                    new DataSourceAssembler().withDataSourceServiceIdentity( "datasource-service" )
                                             .identifiedBy( "testds" )
                                             .visibleIn( Visibility.module )
                                             .withCircuitBreaker( DataSources.newDataSourceCircuitBreaker() )
                                             .assemble( testModule );

                    // Set up Liquibase service that will create the tables
                    new LiquibaseAssembler().identifiedBy( "liquibase1" )
                                            .withConfig( testModule, Visibility.module )
                                            .applyChangelogOnStartup()
                                            .assemble( testModule );
                    testModule.forMixin( LiquibaseConfiguration.class ).declareDefaults()
                              .changeLog().set( "changelog.xml" );
                }

                {
                    ModuleAssembly testModule2 = module.layer().module( "TestDS2" );

                    // Create another specific DataSource that uses the "datasource" service to do the main work
                    // Use DataSourceAssembler to assemble the DataSource.
                    new DataSourceAssembler().withDataSourceServiceIdentity( "datasource-service" )
                                             .identifiedBy( "testds2" )
                                             .visibleIn( Visibility.module )
                                             .withCircuitBreaker( DataSources.newDataSourceCircuitBreaker() )
                                             .assemble( testModule2 );

                    // Set up Liquibase service that will create the tables
                    new LiquibaseAssembler().identifiedBy( "liquibase2" )
                                            .withConfig( testModule2, Visibility.module )
                                            .applyChangelogOnStartup()
                                            .assemble( testModule2 );
                    testModule2.forMixin( LiquibaseConfiguration.class ).declareDefaults()
                              .changeLog().set( "changelog.xml" );
                }

                // START SNIPPET: jmx
                new DataSourceJMXAssembler().visibleIn( Visibility.module ).assemble( module );
                // END SNIPPET: jmx
            }
        };
    }
}
