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

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.sqlkv.assembly.SQLiteEntityStoreAssembler;
import org.apache.polygene.library.sql.assembly.DataSourceAssembler;
import org.apache.polygene.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.polygene.test.entity.model.EntityStoreTestSuite;
import org.jooq.SQLDialect;
import org.junit.After;
import org.junit.BeforeClass;

import static org.apache.polygene.test.util.Assume.assumeNoIbmJdk;

public class SQLiteEntityStoreTestSuite extends EntityStoreTestSuite
{
    @BeforeClass
    public static void beforeClass_IBMJDK()
    {
        assumeNoIbmJdk();
    }

    @Override
    protected void defineStorageModule( ModuleAssembly module )
    {
        module.defaultServices();
        // DataSourceService
        new DBCPDataSourceServiceAssembler()
            .identifiedBy( "sqlite-datasource-service" )
            .visibleIn( Visibility.module )
            .withConfig( configModule, Visibility.application )
            .assemble( module );

        // DataSource
        new DataSourceAssembler()
            .withDataSourceServiceIdentity( "sqlite-datasource-service" )
            .identifiedBy( "sqlite-datasource" )
            .visibleIn( Visibility.module )
            .withCircuitBreaker()
            .assemble( module );

        // SQL EntityStore
        new SQLiteEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( configModule, Visibility.application )
            .assemble( module );
    }

    @Override
    @After
    public void tearDown()
    {
        TearDown.dropTables( application.findModule( INFRASTRUCTURE_LAYER, STORAGE_MODULE ), SQLDialect.SQLITE, super::tearDown );
    }
}
