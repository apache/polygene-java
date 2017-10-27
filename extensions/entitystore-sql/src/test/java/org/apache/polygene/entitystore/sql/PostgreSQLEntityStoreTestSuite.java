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
package org.apache.polygene.entitystore.sql;

import java.util.HashMap;
import java.util.Map;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.sql.assembly.PostgreSQLEntityStoreAssembler;
import org.apache.polygene.library.sql.assembly.DataSourceAssembler;
import org.apache.polygene.library.sql.datasource.DataSourceConfiguration;
import org.apache.polygene.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.polygene.test.docker.DockerRule;
import org.apache.polygene.test.entity.model.EntityStoreTestSuite;
import org.jooq.SQLDialect;
import org.junit.After;
import org.junit.ClassRule;

public class PostgreSQLEntityStoreTestSuite extends EntityStoreTestSuite
{
    @ClassRule
    public static final DockerRule DOCKER;

    static
    {
        Map<String,String> environment = new HashMap<>();
        environment.put( "POSTGRES_USER", System.getProperty( "user.name" ));
        environment.put( "POSTGRES_PASSWORD", "ThisIsGreat!");

        DOCKER = new DockerRule( "postgres",
                                 environment,
                                 3000L,
                                 "PostgreSQL init process complete; ready for start up." );
    }

    @Override
    protected void defineStorageModule( ModuleAssembly module )
    {
        module.defaultServices();
        // DataSourceService
        new DBCPDataSourceServiceAssembler()
            .identifiedBy( "postgresql-datasource-service" )
            .visibleIn( Visibility.module )
            .withConfig( configModule, Visibility.application )
            .assemble( module );

        // DataSource
        new DataSourceAssembler()
            .withDataSourceServiceIdentity( "postgresql-datasource-service" )
            .identifiedBy( "postgresql-datasource" )
            .visibleIn( Visibility.module )
            .withCircuitBreaker()
            .assemble( module );

        // SQL EntityStore
        new PostgreSQLEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( configModule, Visibility.application )
            .assemble( module );

        String host = DOCKER.getDockerHost();
        int port = DOCKER.getExposedContainerPort( "5432/tcp" );
        DataSourceConfiguration defaults = configModule.forMixin( DataSourceConfiguration.class ).declareDefaults();
        defaults.url().set( "jdbc:postgresql://" + host + ":" + port + "/jdbc_test_db" );
        defaults.username().set( System.getProperty( "user.name" ) );
        defaults.password().set( "ThisIsGreat!" );

        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    @Override
    @After
    public void tearDown()
    {
        TearDown.dropTables( application.findModule( INFRASTRUCTURE_LAYER, STORAGE_MODULE ), SQLDialect.POSTGRES, super::tearDown );
    }
}
