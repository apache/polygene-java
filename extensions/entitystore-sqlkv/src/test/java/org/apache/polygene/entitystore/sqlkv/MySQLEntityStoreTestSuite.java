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
package org.apache.polygene.entitystore.sqlkv;

import java.util.HashMap;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.sqlkv.assembly.MySQLEntityStoreAssembler;
import org.apache.polygene.library.sql.assembly.DataSourceAssembler;
import org.apache.polygene.library.sql.datasource.DataSourceConfiguration;
import org.apache.polygene.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.polygene.test.docker.DockerRule;
import org.apache.polygene.test.entity.model.EntityStoreTestSuite;
import org.jooq.SQLDialect;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Ignore;

@Ignore( "Waiting response from JOOQ to fix SQL generation. VARCHAR instead of CHAR")
public class MySQLEntityStoreTestSuite extends EntityStoreTestSuite
{
    @ClassRule
    public static final DockerRule DOCKER = new DockerRule(
        "mysql",
        new HashMap<String, String>()
        {{
            put( "MYSQL_ROOT_PASSWORD", "" );
            put( "MYSQL_ALLOW_EMPTY_PASSWORD", "yes" );
            put( "MYSQL_DATABASE", "jdbc_test_db" );
            put( "MYSQL_ROOT_HOST", "172.17.0.1" );
        }},
        30000L
//        , "mysqld: ready for connections"   TODO: add this after next release of tdomzal/junit-docker-rule
    );

    @Override
    protected void defineStorageModule( ModuleAssembly module )
    {
        module.defaultServices();
        // DataSourceService
        new DBCPDataSourceServiceAssembler()
            .identifiedBy( "mysql-datasource-service" )
            .visibleIn( Visibility.module )
            .withConfig( configModule, Visibility.application )
            .assemble( module );

        // DataSource
        new DataSourceAssembler()
            .withDataSourceServiceIdentity( "mysql-datasource-service" )
            .identifiedBy( "mysql-datasource" )
            .visibleIn( Visibility.module )
            .withCircuitBreaker()
            .assemble( module );

        // SQL EntityStore
        new MySQLEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( configModule, Visibility.application )
            .assemble( module );

        String mysqlHost = DOCKER.getDockerHost();
        int mysqlPort = DOCKER.getExposedContainerPort( "3306/tcp" );
        configModule.forMixin( DataSourceConfiguration.class ).declareDefaults()
                    .url().set( "jdbc:mysql://" + mysqlHost + ":" + mysqlPort
                                + "/jdbc_test_db?profileSQL=false&useLegacyDatetimeCode=false&serverTimezone=UTC"
                                + "&nullCatalogMeansCurrent=true&nullNamePatternMatchesAll=true" );
    }

    @Override
    @After
    public void tearDown()
    {
        TearDown.dropTables( application.findModule( INFRASTRUCTURE_LAYER, STORAGE_MODULE ), SQLDialect.MYSQL, super::tearDown );
    }
}
