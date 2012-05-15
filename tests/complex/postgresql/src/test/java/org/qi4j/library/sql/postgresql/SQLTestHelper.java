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
package org.qi4j.library.sql.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Assume;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.reindexer.ReindexerConfiguration;
import org.qi4j.index.sql.support.common.DBNames;
import org.qi4j.index.sql.support.common.ReindexingStrategy;
import org.qi4j.index.sql.support.postgresql.PostgreSQLAppStartup;
import org.qi4j.index.sql.support.postgresql.assembly.PostgreSQLAssembler;
import org.qi4j.library.sql.assembly.DBCPDataSourceServiceAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.library.sql.datasource.DataSources;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

import org.slf4j.Logger;

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
        ModuleAssembly config = mainModule.layer().module( "config" );
        config.services( MemoryEntityStoreService.class ).visibleIn( Visibility.module );
        
        // EntityStore
        mainModule.services( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );

        // DataSourceService + Index/Query's DataSource
        new DBCPDataSourceServiceAssembler( "datasource-service-postgres",
                                            Visibility.module,
                                            config,
                                            Visibility.layer ).assemble( mainModule );
        DataSourceAssembler dsAssembler = new DataSourceAssembler( "datasource-service-postgres",
                                                                   "datasource-postgres",
                                                                   Visibility.module,
                                                                   DataSources.newDataSourceCircuitBreaker() );

        // Index/Query
        new PostgreSQLAssembler( Visibility.module, dsAssembler ).assemble( mainModule );
        config.entities( SQLConfiguration.class ).visibleIn( Visibility.layer );

        // Always re-index because of possible different app structure of multiple tests.
        mainModule.services( ReindexingStrategy.ReindexingStrategyService.class ).withMixins( ReindexingStrategy.AlwaysNeed.class ).identifiedBy( "reindexer" );
        config.entities( ReindexerConfiguration.class ).visibleIn( Visibility.layer );
    }

    public static void tearDownTest( UnitOfWorkFactory uowf, Module module, Logger log )
    {
        if( uowf == null )
        {
            return;
        }
        if( uowf.currentUnitOfWork() == null )
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                SQLTestHelper.deleteTestData( uow, module );
            }
            catch( Throwable t )
            {
                // Ignore, for now. Happens when assumptions are not true (no DB connection)
                log.error( "Error when deleting test data.", t );
            }
            finally
            {
                uow.discard();
            }
        }
    }

    private static void deleteTestData( UnitOfWork uow, Module module )
        throws SQLException
    {

        SQLConfiguration config = uow.get( SQLConfiguration.class, SQL_INDEXING_SERVICE_NAME );
        Connection connection = module.serviceFinder().findService( DataSource.class).get().getConnection();
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
            DataSource ds = (DataSource) finder.findService( DataSource.class ).get();
            Assume.assumeNotNull( ds.getConnection() );
        }
        catch( Throwable t )
        {
            Assume.assumeNoException( t );
        }
    }

}
