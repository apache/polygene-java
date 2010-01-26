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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.rdf.query.RdfQueryParserFactory;
import org.qi4j.library.rdf.entity.EntityStateParser;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeParser;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.rest.Main;
import org.qi4j.rest.Named;
import org.qi4j.rest.TestEntity;
import org.qi4j.rest.TestEntity2;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.test.AbstractQi4jTest;
import org.restlet.Client;
import org.restlet.Uniform;
import org.restlet.data.Protocol;

import static org.hamcrest.CoreMatchers.*;

/**
 * JAVADOC
 */
public class SPARQLEntityFinderTest
    extends AbstractQi4jTest
{
    ApplicationSPI server;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addEntities( TestEntity.class, TestEntity2.class );
        ModuleAssembly store = module.layerAssembly().moduleAssembly( "REST Store/Finder/Registry" );
        store.addObjects( EntityStateSerializer.class, EntityStateParser.class, EntityTypeSerializer.class, EntityTypeParser.class );
        store.addEntities( RESTEntityStoreConfiguration.class, SPARQLEntityFinderConfiguration.class );
        store.addServices( MemoryEntityStoreService.class );
        store.addServices( RESTEntityStoreService.class, SPARQLEntityFinderService.class, RdfQueryParserFactory.class )
            .visibleIn( Visibility.layer );
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
        super.tearDown();

        if( server != null )
        {
            server.passivate();
        }
    }

    @Test
    @Ignore
    public void testEntityFinder()
    {
        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            try
            {
                Query<Named> query = queryBuilderFactory.newQueryBuilder( Named.class ).newQuery( unitOfWork );
                for( Named testEntity : query )
                {
                    System.out.println( testEntity.name().get() );
                }
                Assert.assertThat( "result size is correct", query.count(), equalTo( 3L ) );
            }
            finally
            {
                unitOfWork.discard();
            }
        }
    }
}
