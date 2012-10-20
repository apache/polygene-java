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
package org.qi4j.entitystore.sql;

import org.apache.derby.iapi.services.io.FileUtil;
import org.junit.BeforeClass;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.sql.assembly.SQLiteEntityStoreAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.qi4j.library.sql.datasource.DataSources;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;

import static org.junit.Assume.assumeTrue;

public class SQLiteEntityStoreTest
        extends AbstractEntityStoreTest
{

    @BeforeClass
    public static void beforeClass_IBMJDK()
    {
        // Ignore this test on IBM JDK
        assumeTrue( !( System.getProperty( "java.vendor" ).contains( "IBM" ) ) );
    }

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        // END SNIPPET: assembly
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        config.services( MemoryEntityStoreService.class );

        // START SNIPPET: assembly
        // DataSourceService + EntityStore's DataSource using DBCP connection pool
        new DBCPDataSourceServiceAssembler( "sqlite-datasource-service",
                                            Visibility.module,
                                            config,
                                            Visibility.layer ).assemble( module );
        DataSourceAssembler dsAssembler = new DataSourceAssembler( "sqlite-datasource-service",
                                                                   "sqlite-datasource",
                                                                   Visibility.module,
                                                                   DataSources.newDataSourceCircuitBreaker() );

        // SQL EntityStore
        new SQLiteEntityStoreAssembler( dsAssembler ).assemble( module );
        config.entities( SQLConfiguration.class ).visibleIn( Visibility.layer );
    }
    // END SNIPPET: assembly

    @Override
    public void tearDown()
            throws Exception
    {
        if ( module == null ) {
            return;
        }
        UnitOfWork uow = this.module.newUnitOfWork( UsecaseBuilder.newUsecase( "Delete " + getClass().getSimpleName() + " test data" ) );
        try {

            FileUtil.removeDirectory( "target/qi4j-data" );

        } finally {
            uow.discard();
            super.tearDown();
        }
    }

}
