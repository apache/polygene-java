/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.test.performance.entitystore.sql;

import org.apache.derby.iapi.services.io.FileUtil;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.sql.assembly.DerbySQLEntityStoreAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.test.performance.entitystore.model.AbstractEntityStorePerformanceTest;

/**
 * Performance test for SQLEntityStoreComposite
 */
public class DerbySQLEntityStorePerformanceTest
        extends AbstractEntityStorePerformanceTest
{

    public DerbySQLEntityStorePerformanceTest()
    {
        super( "DerbySQLEntityStore", createAssembler() );
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
                        identifiedBy( "derby-datasource-service" ).
                        visibleIn( Visibility.module ).
                        withConfig( config ).
                        withConfigVisibility( Visibility.layer ).
                        assemble( module );

                // DataSource
                new DataSourceAssembler().
                        withDataSourceServiceIdentity( "derby-datasource-service" ).
                        identifiedBy( "derby-datasource" ).
                        withCircuitBreaker().
                        assemble( module );

                // SQL EntityStore
                new DerbySQLEntityStoreAssembler().
                        withConfig( config ).
                        withConfigVisibility( Visibility.layer ).
                        assemble( module );
            }

        };
    }

    @Test
    @Override
    public void whenCreateEntityWithSinglePropertyThenRecordIterationsPerSecond()
            throws Exception
    {
        super.whenCreateEntityWithSinglePropertyThenRecordIterationsPerSecond();
    }

    @Test
    @Override
    public void whenCreateEntityWithSinglePropertyInBatchThenRecordIterationsPerSecond()
            throws Exception
    {
        super.whenCreateEntityWithSinglePropertyInBatchThenRecordIterationsPerSecond();
    }

    public void ____cleanUp()
            throws Exception
    {
        super.cleanUp();
        FileUtil.removeDirectory( "target/qi4j-data" );
    }

}
