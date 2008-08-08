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

import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.library.rdf.entity.EntityParserService;
import org.qi4j.rest.TestEntity;
import org.qi4j.structure.Visibility;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class RESTEntityStoreTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addEntities( TestEntity.class );

        ModuleAssembly store = module.getLayerAssembly().newModuleAssembly( "REST Store" );
        store.addEntities( RESTEntityStoreConfiguration.class );
        store.addServices( MemoryEntityStoreService.class, EntityParserService.class );
        store.addServices( RESTEntityStoreService.class ).visibleIn( Visibility.layer );
    }

    @Test
    public void testEntityStore()
    {
        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            try
            {
                TestEntity entity = unitOfWork.find( "test2", TestEntity.class );
                System.out.println( entity.test1().get() );
                TestEntity testEntity = entity.association().get();
                System.out.println( testEntity.test1().get() );

                unitOfWork.discard();
            }
            catch( Exception e )
            {
                unitOfWork.discard();
            }
        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            try
            {
                TestEntity entity = unitOfWork.find( "test2", TestEntity.class );
                System.out.println( entity.test1().get() );
                System.out.println( entity.association().get().test1().get() );

                unitOfWork.discard();
            }
            catch( Exception e )
            {
                unitOfWork.discard();
            }
        }

    }
}
