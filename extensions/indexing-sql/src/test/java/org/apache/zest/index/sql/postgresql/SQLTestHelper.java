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
package org.apache.zest.index.sql.postgresql;

import java.sql.Connection;
import javax.sql.DataSource;
import org.apache.zest.api.service.ServiceFinder;
import org.junit.Assume;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.structure.Module;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.reindexer.ReindexerConfiguration;
import org.apache.zest.index.sql.assembly.PostgreSQLIndexQueryAssembler;
import org.apache.zest.index.sql.support.common.RebuildingStrategy;
import org.apache.zest.index.sql.support.common.ReindexingStrategy;
import org.apache.zest.library.sql.assembly.DataSourceAssembler;
import org.apache.zest.library.sql.common.SQLUtil;
import org.apache.zest.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.zest.test.EntityTestAssembler;

public class SQLTestHelper
{

    public static final String SEPARATE_MODULE_NAME = "actual_module";

    public static void assembleWithMemoryEntityStore( ModuleAssembly mainModule )
        throws AssemblyException
    {
        // EntityStore
        new EntityTestAssembler().visibleIn( Visibility.application ).assemble( mainModule );

        doCommonAssembling( mainModule );
    }

    protected static void doCommonAssembling( ModuleAssembly mainModule )
        throws AssemblyException
    {
        ModuleAssembly config = mainModule.layer().module( "config" );
        new EntityTestAssembler().assemble( config );

        // START SNIPPET: assembly
        // DataSourceService
        new DBCPDataSourceServiceAssembler().
            identifiedBy( "postgres-datasource-service" ).
            visibleIn( Visibility.module ).
            withConfig( config, Visibility.layer ).
            assemble( mainModule );

        // DataSource
        new DataSourceAssembler().
            withDataSourceServiceIdentity( "postgres-datasource-service" ).
            identifiedBy( "postgres-datasource" ).
            visibleIn( Visibility.module ).
            withCircuitBreaker().
            assemble( mainModule );

        // SQL Index/Query
        new PostgreSQLIndexQueryAssembler().
            visibleIn( Visibility.module ).
            withConfig( config, Visibility.layer ).
            assemble( mainModule );
        // END SNIPPET: assembly

        // Always re-build schema in test scenarios because of possibly different app structure in
        // various tests
        mainModule.services( RebuildingStrategy.class ).
            withMixins( RebuildingStrategy.AlwaysNeed.class ).
            visibleIn( Visibility.module );

        // Always re-index in test scenarios
        mainModule.services( ReindexingStrategy.class ).
            withMixins( ReindexingStrategy.AlwaysNeed.class ).
            visibleIn( Visibility.module );
        config.entities( ReindexerConfiguration.class ).
            visibleIn( Visibility.layer );
    }

    public static void setUpTest( ServiceFinder serviceFinder )
    {
        Connection connection = null;
        try
        {

            DataSource ds = serviceFinder.findService( DataSource.class ).get();
            connection = ds.getConnection();
            Assume.assumeNotNull( connection );

        }
        catch( Throwable t )
        {

            t.printStackTrace();
            Assume.assumeNoException( t );

        }
        finally
        {

            SQLUtil.closeQuietly( connection );

        }
    }

    private SQLTestHelper()
    {
    }

}
