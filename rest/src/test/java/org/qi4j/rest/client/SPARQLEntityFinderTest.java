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

import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.library.rdf.entity.EntitySerializer;
import org.qi4j.library.rdf.entity.EntityParser;
import org.qi4j.api.query.Query;
import org.qi4j.rest.Main;
import org.qi4j.rest.TestEntity;
import org.qi4j.api.structure.Application;
import org.qi4j.api.common.Visibility;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class SPARQLEntityFinderTest
    extends AbstractQi4jTest
{
    Application server;

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addEntities( TestEntity.class );

        ModuleAssembly store = module.layerAssembly().newModuleAssembly( "REST Store/Finder" );
        store.addObjects( EntitySerializer.class, EntityParser.class);
        store.addEntities( RESTEntityStoreConfiguration.class, SPARQLEntityFinderConfiguration.class );
        store.addServices( MemoryEntityStoreService.class, RestletClientService.class );
        store.addServices( RESTEntityStoreService.class, SPARQLEntityFinderService.class ).visibleIn( Visibility.layer );
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
    public void testEntityFinder()
    {
        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            try
            {
                Query<TestEntity> query = unitOfWork.queryBuilderFactory().newQueryBuilder( TestEntity.class ).newQuery();
                for( TestEntity testEntity : query )
                {
                    System.out.println( testEntity.name().get() );
                }
                Assert.assertThat( "result size is correct", query.count(), equalTo( 2L ) );
            }
            finally
            {
                unitOfWork.discard();
            }
        }
    }
}