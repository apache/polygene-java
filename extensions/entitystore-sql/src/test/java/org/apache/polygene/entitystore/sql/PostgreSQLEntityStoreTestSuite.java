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

import com.github.junit5docker.Docker;
import com.github.junit5docker.Environment;
import com.github.junit5docker.Port;
import com.github.junit5docker.WaitFor;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.sql.assembly.PostgreSQLEntityStoreAssembler;
import org.apache.polygene.library.sql.assembly.DataSourceAssembler;
import org.apache.polygene.library.sql.datasource.DataSourceConfiguration;
import org.apache.polygene.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.polygene.test.entity.model.EntityStoreTestSuite;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.AfterEach;

@Docker( image = "org.apache.polygene:org.apache.polygene.internal.docker-postgres",
         ports = @Port( exposed = 8801, inner = 5432 ),
         waitFor = @WaitFor( value = "PostgreSQL init process complete; ready for start up.", timeoutInMillis = 30000 ),
         newForEachCase = false,
         environments = {
             @Environment( key = "POSTGRES_USER", value = "polygene" ),
             @Environment( key = "POSTGRES_PASSWORD", value = "ThisIsGreat!" )
         }
)
public class PostgreSQLEntityStoreTestSuite extends EntityStoreTestSuite
{
    @Override
    protected void defineStorageModule( ModuleAssembly module )
    {
        PostgreSQLEntityStoreTest.delay();
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

        String host = "localhost";
        int port = 8801;
        DataSourceConfiguration defaults = configModule.forMixin( DataSourceConfiguration.class ).declareDefaults();
        defaults.url().set( "jdbc:postgresql://" + host + ":" + port + "/jdbc_test_db" );
        defaults.username().set( "polygene" );
        defaults.password().set( "ThisIsGreat!" );

        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    @AfterEach
    public void cleanUpData()
    {
        TearDown.dropTables( application.findModule( INFRASTRUCTURE_LAYER, STORAGE_MODULE ), SQLDialect.POSTGRES, super::tearDown );
    }
}
