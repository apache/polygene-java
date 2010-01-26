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
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.library.rdf.entity.EntityStateParser;
import org.qi4j.library.rdf.entity.EntityTypeParser;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.rest.Main;
import org.qi4j.rest.TestEntity;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.test.AbstractQi4jTest;
import org.restlet.Client;
import org.restlet.Uniform;
import org.restlet.data.Protocol;

/**
 * JAVADOC
 */
public class RESTEntityStoreTest
    extends AbstractQi4jTest
{
    ApplicationSPI server;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addEntities( TestEntity.class );

        ModuleAssembly store = module.layerAssembly().moduleAssembly( "REST Store" );
        store.addObjects( EntityStateParser.class, EntityTypeParser.class, EntityTypeSerializer.class );
        store.addEntities( RESTEntityStoreConfiguration.class );
        store.addServices( MemoryEntityStoreService.class );
        store.addServices( RESTEntityStoreService.class ).visibleIn( Visibility.layer );
        store.importServices( Uniform.class );
        try
        {
            Client client = new Client( Protocol.HTTP );
            client.start();
            module.layerAssembly().applicationAssembly().setMetaInfo( client );
        }
        catch( Exception e )
        {
            throw new AssemblyException( "Problem to start Sparql client.", e );
        }
    }

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        server = new Main().application();

        super.setUp();
    }

    @Override
    @After
    public void tearDown()
        throws Exception
    {
        Thread.sleep( 1000 );
        super.tearDown();
        Thread.sleep( 1000 );
        server.passivate();
    }

    @Test
    @Ignore( "I can't get this test to run reliably on the SRV03 release machine. Broken Pipe as a SocketException." )
    public void testEntityStore()
        throws UnitOfWorkCompletionException
    {
        // Create state
        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            TestEntity entity = unitOfWork.newEntity( TestEntity.class, "test4" );
            entity.name().set( "Rickard" );
            entity.age().set( 42 );
            unitOfWork.complete();
        }

        // Load state
        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            try
            {
                TestEntity entity = unitOfWork.get( TestEntity.class, "test2" );
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
                TestEntity entity = unitOfWork.get( TestEntity.class, "test2" );
                entity.name().set( "Foo bar" );
                System.out.println( entity.rdfAssociation().contains( entity ) );
                entity.rdfAssociation().add( 0, entity );
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
                TestEntity entity = unitOfWork.get( TestEntity.class, "test2" );
                System.out.println( entity.name().get() );
                System.out.println( entity.association().get().name().get() );
                System.out.println( entity.rdfAssociation().contains( entity ) );
            }
            finally
            {
                unitOfWork.discard();
            }
        }
    }
}
