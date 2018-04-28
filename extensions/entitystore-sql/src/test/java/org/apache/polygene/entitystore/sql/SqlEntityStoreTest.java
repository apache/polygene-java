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

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.sql.assembly.H2SQLEntityStoreAssembler;
import org.apache.polygene.library.sql.assembly.DataSourceAssembler;
import org.apache.polygene.library.sql.datasource.DataSourceConfiguration;
import org.apache.polygene.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.TemporaryFolder;
import org.apache.polygene.test.entity.AbstractEntityStoreTest;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith( TemporaryFolder.class )
public class SqlEntityStoreTest extends AbstractEntityStoreTest
{
    private TemporaryFolder tmpDir;

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws Exception
    {
        // END SNIPPET: assembly
        super.assemble( module );
        module.defaultServices();
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().visibleIn( Visibility.module ).assemble( config );

        // START SNIPPET: assembly
        // Assemble a DataSource
        new DataSourceAssembler()
            .withDataSourceServiceIdentity( "datasource" )
            .identifiedBy( "ds-h2" )
            .visibleIn( Visibility.module )
            .assemble( module );

        // Assemble the Apache DBCP based Service Importer
        new DBCPDataSourceServiceAssembler()
            .identifiedBy( "datasource" )
            .visibleIn( Visibility.module )
            .withConfig( config, Visibility.layer )
            .assemble( module );

        new H2SQLEntityStoreAssembler()
            .withConfig( config, Visibility.layer )
            .identifiedBy( "sql-entitystore" )
            .assemble( module );
        // END SNIPPET: assembly

        SqlEntityStoreConfiguration jooqDefaults = config.forMixin( SqlEntityStoreConfiguration.class )
                                                         .setMetaInfo( SQLDialect.H2 )
                                                         .declareDefaults();
        jooqDefaults.entitiesTableName().set( "ENTITIES" );

        DataSourceConfiguration dsDefaults = config.forMixin( DataSourceConfiguration.class ).declareDefaults();
        dsDefaults.driver().set( org.h2.Driver.class.getName() );
        dsDefaults.enabled().set( true );
        dsDefaults.maxPoolSize().set( 3 );
        dsDefaults.minPoolSize().set( 1 );
        dsDefaults.username().set( "" );
        dsDefaults.password().set( "" );
        dsDefaults.url().set( "jdbc:h2:" + tmpDir.getRoot().getAbsolutePath() + "/testdb;create=true" );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    @AfterEach
    public void cleanUpData()
    {
        TearDown.dropTables( moduleInstance, SQLDialect.DERBY, super::tearDown );
    }
}
