/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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
package org.qi4j.index.sql.postgresql;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.Assume;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.reindexer.ReindexerConfiguration;
import org.qi4j.index.sql.assembly.PostgreSQLIndexQueryAssembler;
import org.qi4j.index.sql.support.common.RebuildingStrategy;
import org.qi4j.index.sql.support.common.ReindexingStrategy;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.test.EntityTestAssembler;

public class SQLTestHelper
{

    public static final String SEPARATE_MODULE_NAME = "actual_module";

    public static void assembleWithMemoryEntityStore( ModuleAssembly mainModule )
        throws AssemblyException
    {
        // EntityStore
        new EntityTestAssembler( Visibility.application ).assemble( mainModule );

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
                withConfig( config ).
                withConfigVisibility( Visibility.layer ).
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
                visibleIn( Visibility.application ).
                withConfig( config ).
                withConfigVisibility( Visibility.layer ).
                assemble( mainModule );
        // END SNIPPET: assembly

        // Always re-build schema in test scenarios because of possibly different app structure in
        // various tests
        mainModule.services( RebuildingStrategy.class )
            .withMixins( RebuildingStrategy.AlwaysNeed.class ).visibleIn( Visibility.module );

        // Always re-index in test scenarios
        mainModule.services( ReindexingStrategy.class ).withMixins(
            ReindexingStrategy.AlwaysNeed.class ).visibleIn( Visibility.module );
        config.entities( ReindexerConfiguration.class ).visibleIn( Visibility.layer );
    }

    public static void setUpTest( Module module )
    {
        Connection connection = null;
        try
        {

            DataSource ds = module.findService( DataSource.class ).get();
            connection = ds.getConnection();
            Assume.assumeNotNull( connection );

        }
        catch ( Throwable t )
        {

            t.printStackTrace();
            Assume.assumeNoException( t );

        }
        finally
        {

            SQLUtil.closeQuietly( connection );

        }
    }

}
