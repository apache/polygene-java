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
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.sql.assembly.MariaDbSQLEntityStoreAssembler;
import org.apache.polygene.library.sql.assembly.DataSourceAssembler;
import org.apache.polygene.library.sql.datasource.DataSourceConfiguration;
import org.apache.polygene.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.entity.AbstractEntityStoreTest;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;

@Disabled( "Waiting response from JOOQ to fix SQL generation. VARCHAR instead of CHAR")
@Docker( image = "mariadb",
         ports = @Port( exposed = 8801, inner = 3306 ),
         environments = {
             @Environment( key = "MYSQL_ROOT_PASSWORD", value = "" ),
             @Environment( key = "MYSQL_ALLOW_EMPTY_PASSWORD", value = "yes" ),
             @Environment( key = "MYSQL_DATABASE", value = "jdbc_test_db" )
         },
         waitFor = @WaitFor( value = "mysqld: ready for connections", timeoutInMillis = 30000 ),
         newForEachCase = false
)
public class MariaDbEntityStoreTest
    extends AbstractEntityStoreTest
{

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // END SNIPPET: assembly
        sleep();
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );

        // START SNIPPET: assembly
        // DataSourceService
        new DBCPDataSourceServiceAssembler()
            .identifiedBy( "mariadb-datasource-service" )
            .visibleIn( Visibility.module )
            .withConfig( config, Visibility.layer )
            .assemble( module );

        // DataSource
        new DataSourceAssembler()
            .withDataSourceServiceIdentity( "mariadb-datasource-service" )
            .identifiedBy( "mariadb-datasource" )
            .visibleIn( Visibility.module )
            .withCircuitBreaker()
            .assemble( module );

        // SQL EntityStore
        new MariaDbSQLEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( config, Visibility.layer )
            .assemble( module );
        // END SNIPPET: assembly
        String host = "localhost";
        int port = 8801;
        DataSourceConfiguration defaults = config.forMixin( DataSourceConfiguration.class ).declareDefaults();
        defaults.url().set( "jdbc:mysql://" + host + ":" + port
                            + "/jdbc_test_db?profileSQL=false&useLegacyDatetimeCode=false&serverTimezone=UTC"
                            + "&nullCatalogMeansCurrent=true&nullNamePatternMatchesAll=true" );
        defaults.driver().set( "com.mysql.jdbc.Driver" );
        defaults.enabled().set( true );
        defaults.username().set("root");
        defaults.password().set( "" );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    static void sleep()
    {
        try
        {
            Thread.sleep( 11500 );
        }
        catch( InterruptedException e )
        {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void cleanUpData()
    {
        TearDown.dropTables( moduleInstance, SQLDialect.MARIADB, super::tearDown );
    }
}
