/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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
package org.qi4j.library.sql.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assume;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.reindexer.ReindexerConfiguration;
import org.qi4j.index.sql.assembly.SQLIndexingAssembler;
import org.qi4j.index.sql.support.common.DBNames;
import org.qi4j.index.sql.support.common.ReindexingStrategy;
import org.qi4j.index.sql.support.postgresql.PostgreSQLAppStartup;
import org.qi4j.index.sql.support.postgresql.assembly.PostgreSQLAssembler;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.library.sql.ds.DataSourceService;
import org.qi4j.library.sql.ds.PGDataSourceConfiguration;
import org.qi4j.library.sql.ds.PGSQLDataSourceServiceMixin;
import org.qi4j.library.sql.ds.assembly.DataSourceAssembler;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.slf4j.Logger;

/**
 * @author Stanislav Muhametsin
 */
public class SQLTestHelper
{
    public static final String SQL_INDEXING_SERVICE_NAME = PostgreSQLAssembler.INDEXING_SERVICE_NAME;

    public static final String CONFIG_MODULE_NAME = "config_module";

    public static final String SEPARATE_MODULE_NAME = "actual_module";

    public static void assembleWithMemoryEntityStore( ModuleAssembly mainModule )
        throws AssemblyException
    {
        doCommonAssembling( mainModule );

    }

    public static void assembleWithSQLEntityStore( ModuleAssembly mainModule )
        throws AssemblyException
    {
        doCommonAssembling( mainModule );
    }

    protected static void doCommonAssembling( ModuleAssembly mainModule )
        throws AssemblyException
    {
        ModuleAssembly configModule = mainModule.layer().module( CONFIG_MODULE_NAME );
        configModule
            .entities( PGDataSourceConfiguration.class, SQLConfiguration.class, ReindexerConfiguration.class )
            .visibleIn( Visibility.application );
        configModule.services( MemoryEntityStoreService.class );

        PostgreSQLAssembler pgAss = new PostgreSQLAssembler( Visibility.module, new DataSourceAssembler(
            PGSQLDataSourceServiceMixin.class ).setDataSourceServiceName( PostgreSQLAssembler.DATASOURCE_SERVICE_NAME ) )
            .setServiceName( SQL_INDEXING_SERVICE_NAME );
        pgAss.assemble( mainModule );

        SQLIndexingAssembler ass = new SQLIndexingAssembler( Visibility.module );
        ass.assemble( mainModule );

        // Always re-index because of possible different app structure of multiple tests.
        mainModule.services( ReindexingStrategy.ReindexingStrategyService.class ).withMixins(
            ReindexingStrategy.AlwaysNeed.class );

        mainModule.services( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class ).visibleIn(
            Visibility.application );
    }

    public static void tearDownTest( UnitOfWorkFactory uowf, ServiceFinder finder, Logger log )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        Boolean created = false;
        if( uowf.currentUnitOfWork() == null )
        {
            uow = uowf.newUnitOfWork();
            created = true;
        }

        try
        {
            SQLTestHelper.deleteTestData( uow, finder );
        }
        catch( Throwable t )
        {
            // Ignore, for now. Happens when assumptions are not true (no DB connection)
            // log.error( "Error when deleting test data.", t );
        }
        finally
        {
            if( created )
            {
                uow.discard();
            }
        }
    }

    private static void deleteTestData( UnitOfWork uow, ServiceFinder finder )
        throws SQLException
    {

        SQLConfiguration config = uow.get( SQLConfiguration.class, SQL_INDEXING_SERVICE_NAME );
        Connection connection = SQLUtil.getConnection( finder );
        String schemaName = config.schemaName().get();
        if( schemaName == null )
        {
            schemaName = PostgreSQLAppStartup.DEFAULT_SCHEMA_NAME;
        }

        Statement stmt = null;
        try
        {
            stmt = connection.createStatement();
            stmt.execute( String.format( "DELETE FROM %s." + DBNames.ENTITY_TABLE_NAME, schemaName ) );
            connection.commit();
        }
        finally
        {
            SQLUtil.closeQuietly( stmt );
        }
    }

    public static void setUpTest( ServiceFinder finder )
    {
        try
        {
            DataSourceService ds = (DataSourceService) finder.findService( DataSourceService.class ).get();
            Assume.assumeNotNull( ds.getDataSource().getConnection() );
        }
        catch( Throwable t )
        {
            t.printStackTrace();
            Assume.assumeNoException( t );
        }
    }

}
