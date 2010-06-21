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

import org.qi4j.entitystore.sql.bootstrap.MySQLMapEntityStoreAssembler;
import org.qi4j.entitystore.sql.map.database.DatabaseConfiguration;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.test.entity.AbstractEntityStoreTest;

@Ignore // Needs external setup
public class MySQLMapEntityStoreTest
        extends AbstractEntityStoreTest
{

    @Override
    @SuppressWarnings( "unchecked" )
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        super.assemble( module );
        new MySQLMapEntityStoreAssembler().assemble( module );
        ModuleAssembly config = module.layerAssembly().moduleAssembly( "config" );
        config.addServices( MemoryEntityStoreService.class );
        config.addEntities( DatabaseConfiguration.class ).visibleIn( Visibility.layer );
    }

    @Test
    @Override
    public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
            throws UnitOfWorkCompletionException
    {
        super.givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification();
    }

    @After
    @Override
    public void tearDown()
            throws Exception
    {
        super.tearDown();
        // TODO : delete test data
    }

}
