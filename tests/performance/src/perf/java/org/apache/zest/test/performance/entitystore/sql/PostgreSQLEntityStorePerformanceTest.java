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
 */
package org.apache.polygene.test.performance.entitystore.sql;

import java.sql.Connection;
import java.sql.Statement;
import org.junit.Ignore;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.bootstrap.ApplicationAssemblerAdapter;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.Energy4Java;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.memory.MemoryEntityStoreService;
import org.apache.polygene.entitystore.sql.assembly.PostgreSQLEntityStoreAssembler;
import org.apache.polygene.entitystore.sql.internal.SQLs;
import org.apache.polygene.library.sql.assembly.DataSourceAssembler;
import org.apache.polygene.library.sql.common.SQLConfiguration;
import org.apache.polygene.library.sql.common.SQLUtil;
import org.apache.polygene.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.polygene.test.performance.entitystore.AbstractEntityStorePerformanceTest;

/**
 * Performance test for PostgreSQLEntityStore.
 * <p>
 * WARN This test is deactivated on purpose, please do not commit it activated.
 * </p>
 * <p>
 * To run it see PostgreSQLEntityStoreTest.
 * </p>
 */
@Ignore( "WARN Tearing down this test is broken!" )
public class PostgreSQLEntityStorePerformanceTest
    extends AbstractEntityStorePerformanceTest
{

    public PostgreSQLEntityStorePerformanceTest()
    {
        super( "PostgreSQLEntityStore", createAssembler() );
    }

    private static Assembler createAssembler()
    {
        return new Assembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                ModuleAssembly config = module.layer().module( "config" );
                config.services( MemoryEntityStoreService.class );

                // DataSourceService
                new DBCPDataSourceServiceAssembler().
                    identifiedBy( "postgresql-datasource-service" ).
                    visibleIn( Visibility.module ).
                    withConfig( config, Visibility.layer ).
                    assemble( module );

                // DataSource
                new DataSourceAssembler().
                    withDataSourceServiceIdentity( "postgresql-datasource-service" ).
                    identifiedBy( "postgresql-datasource" ).
                    withCircuitBreaker().
                    assemble( module );

                // SQL EntityStore
                new PostgreSQLEntityStoreAssembler().
                    withConfig( config, Visibility.layer ).
                    assemble( module );
            }

        };
    }

    @Override
    protected void cleanUp()
        throws Exception
    {
        try
        {
            super.cleanUp();
        }
        finally
        {

            Energy4Java zest = new Energy4Java();
            Assembler[][][] assemblers = new Assembler[][][]
            {
                {
                    {
                        createAssembler()
                    }
                }
            };
            Application application = zest.newApplication( new ApplicationAssemblerAdapter( assemblers )
            {
            } );
            application.activate();

            Module moduleInstance = application.findModule( "Layer 1", "config" );
            UnitOfWorkFactory uowf = moduleInstance.unitOfWorkFactory();
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                SQLConfiguration config = uow.get( SQLConfiguration.class,
                                                   PostgreSQLEntityStoreAssembler.DEFAULT_ENTITYSTORE_IDENTITY );
                // TODO fix AbstractEntityStorePerformanceTest to extend from AbstractPolygeneTest
                Connection connection = null; // SQLUtil.getConnection( this.serviceLocator );
                String schemaName = config.schemaName().get();
                if( schemaName == null )
                {
                    schemaName = SQLs.DEFAULT_SCHEMA_NAME;
                }

                Statement stmt = null;
                try
                {
                    stmt = connection.createStatement();
                    stmt.execute( String.format( "DELETE FROM %s." + SQLs.TABLE_NAME, schemaName ) );
                    connection.commit();
                }
                finally
                {
                    SQLUtil.closeQuietly( stmt );
                }
            }
            finally
            {
                uow.discard();
            }
        }
    }

}
