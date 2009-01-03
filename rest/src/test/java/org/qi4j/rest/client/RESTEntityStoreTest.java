/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.rest.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.library.rdf.entity.EntityStateParser;
import org.qi4j.rest.Main;
import org.qi4j.rest.TestEntity;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class RESTEntityStoreTest
    extends AbstractQi4jTest
{
    Application server;

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addEntities( TestEntity.class );

        ModuleAssembly store = module.layerAssembly().newModuleAssembly( "REST Store" );
        store.addObjects( EntityStateParser.class );
        store.addEntities( RESTEntityStoreConfiguration.class );
        store.addServices( MemoryEntityStoreService.class, RestletClientService.class );
        store.addServices( RESTEntityStoreService.class ).visibleIn( Visibility.layer );
    }

    @Override @Before public void setUp() throws Exception
    {
        server = new Main().application();

        super.setUp();
    }

    @Override @After public void tearDown() throws Exception
    {
        super.tearDown();

        server.passivate();
    }

    @Test
    public void testEntityStore() throws UnitOfWorkCompletionException
    {
        // Load state
        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            try
            {
                TestEntity entity = unitOfWork.find( "test2", TestEntity.class );
                System.out.println( entity.name().get() );
                TestEntity testEntity = entity.association().get();
                System.out.println( testEntity.name().get() );
            }
            finally
            {
                unitOfWork.discard();
            }
        }

        // Change state
        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            try
            {
                TestEntity entity = unitOfWork.find( "test2", TestEntity.class );
                entity.name().set( "Foo bar" );
                System.out.println( entity.listAssociation().contains( entity ) );
                entity.listAssociation().add( entity );
                unitOfWork.complete();
            }
            finally
            {
                unitOfWork.discard();
            }
        }

        // Load it again
        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            try
            {
                TestEntity entity = unitOfWork.find( "test2", TestEntity.class );
                System.out.println( entity.name().get() );
                System.out.println( entity.association().get().name().get() );
                System.out.println( entity.listAssociation().contains( entity ) );
            }
            finally
            {
                unitOfWork.discard();
            }
        }

    }
}
