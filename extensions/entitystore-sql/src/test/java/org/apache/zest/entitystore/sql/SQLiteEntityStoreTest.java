/*
 * Copyright (c) 2012, Paul Merlin.
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
package org.apache.zest.entitystore.sql;

import java.io.File;
import org.apache.derby.iapi.services.io.FileUtil;
import org.junit.BeforeClass;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.sql.assembly.SQLiteEntityStoreAssembler;
import org.apache.zest.library.sql.assembly.DataSourceAssembler;
import org.apache.zest.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.entity.AbstractEntityStoreTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

import static org.apache.zest.test.util.Assume.assumeNoIbmJdk;

public class SQLiteEntityStoreTest
    extends AbstractEntityStoreTest
{
    @BeforeClass
    public static void beforeClass_IBMJDK()
    {
        assumeNoIbmJdk();
    }

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // END SNIPPET: assembly
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );
        new OrgJsonValueSerializationAssembler().assemble( module );

        // START SNIPPET: assembly
        // DataSourceService
        new DBCPDataSourceServiceAssembler().
            identifiedBy( "sqlite-datasource-service" ).
            visibleIn( Visibility.module ).
            withConfig( config, Visibility.layer ).
            assemble( module );

        // DataSource
        new DataSourceAssembler().
            withDataSourceServiceIdentity( "sqlite-datasource-service" ).
            identifiedBy( "sqlite-datasource" ).
            visibleIn( Visibility.module ).
            withCircuitBreaker().
            assemble( module );

        // SQL EntityStore
        new SQLiteEntityStoreAssembler().
            visibleIn( Visibility.application ).
            withConfig( config, Visibility.layer ).
            assemble( module );
    }
    // END SNIPPET: assembly

    @Override
    public void tearDown()
        throws Exception
    {
        try
        {
            FileUtil.removeDirectory( new File( "target/zest-data" ) );
        }
        finally
        {
            super.tearDown();
        }
    }

}
