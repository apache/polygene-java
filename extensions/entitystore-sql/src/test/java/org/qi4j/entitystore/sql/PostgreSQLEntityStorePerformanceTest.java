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
package org.qi4j.entitystore.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.Ignore;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssemblerAdapter;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.sql.bootstrap.PostgreSQLEntityStoreAssembler;
import org.qi4j.entitystore.sql.database.PostgreSQLConfiguration;
import org.qi4j.entitystore.sql.database.SQLs;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.test.entity.performance.AbstractEntityStorePerformanceTest;

/**
 *
 * @author Stanislav Muhametsin
 */
@Ignore // ES Performance tests seem to be pretty broken at the moment. At least if I want to delete test data after running tests, and also by not waiting for Runnables to finish running.
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

            @SuppressWarnings( "unchecked" )
            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                new PostgreSQLEntityStoreAssembler().assemble( module );
                ModuleAssembly configModule = module.layerAssembly().moduleAssembly( "config" );
                configModule.addServices( MemoryEntityStoreService.class );
                configModule.addEntities( PostgreSQLConfiguration.class ).visibleIn( Visibility.layer );
            }

        };
    }

    @Override
    protected void cleanUp()
            throws Exception
    {
        try {
            super.cleanUp();
        } finally {

            Energy4Java qi4j = new Energy4Java();
            Assembler[][][] assemblers = new Assembler[][][]{
                {
                    {
                        createAssembler()
                    }
                }
            };
            ApplicationSPI application = qi4j.newApplication( new ApplicationAssemblerAdapter( assemblers )
            {
            } );
            application.activate();

            Module moduleInstance = application.findModule( "Layer 1", "config" );
            UnitOfWorkFactory uowf = moduleInstance.unitOfWorkFactory();
            UnitOfWork uow = uowf.newUnitOfWork();
            try {
                PostgreSQLConfiguration config = uow.get( PostgreSQLConfiguration.class, PostgreSQLEntityStoreAssembler.SERVICE_NAME );
                Connection connection = DriverManager.getConnection( config.connectionString().get() );
                String schemaName = config.schemaName().get();
                if ( schemaName == null ) {
                    schemaName = SQLs.DEFAULT_SCHEMA_NAME;
                }

                Statement stmt = null;
                try {
                    stmt = connection.createStatement();
                    stmt.execute( String.format( "DELETE FROM %s." + SQLs.TABLE_NAME, schemaName ) );
                    connection.commit();
                } finally {
                    SQLUtil.closeQuietly( stmt );
                }
            } finally {
                uow.discard();
            }
        }
    }

}
