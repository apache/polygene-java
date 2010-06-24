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
package org.qi4j.entitystore.sql;

import org.qi4j.entitystore.sql.bootstrap.DerbySQLMapEntityStoreAssembler;
import org.qi4j.entitystore.sql.map.database.DatabaseConfiguration;
import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.performance.AbstractEntityStorePerformanceTest;

/**
 * Performance test for SQLEntityStoreComposite
 */
public class DerbySQLMapEntityStorePerformanceTest
        extends AbstractEntityStorePerformanceTest
{

    public DerbySQLMapEntityStorePerformanceTest()
    {
        super( "DerbySQLEntityStore", createAssembler() );
    }

    private static Assembler createAssembler()
    {
        return new Assembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                new DerbySQLMapEntityStoreAssembler( Visibility.application ).assemble( module );
                ModuleAssembly configModule = module.layerAssembly().moduleAssembly( "Config" );
                configModule.addEntities( DatabaseConfiguration.class ).visibleIn( Visibility.layer );
                new EntityTestAssembler( Visibility.module ).assemble( configModule );
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
        File dbFile = new File( "target/jdbmstore.data.db" );
        boolean success = true;
        if ( dbFile.exists() ) {
            success = dbFile.delete();
        }

        File logFile = new File( "qi4j/jdbmstore.data.lg" );
        if ( logFile.exists() ) {
            success = success & logFile.delete();
        }
        if ( !success ) {
            throw new Exception( "Could not delete test data" );
        }
    }

}
