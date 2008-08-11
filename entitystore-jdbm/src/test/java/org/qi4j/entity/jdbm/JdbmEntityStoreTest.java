/*  Copyright 2008 Rickard Öberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.jdbm;

import java.io.File;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.ConcurrentEntityModificationException;
import org.qi4j.entity.EntityBuilder;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.structure.Visibility;
import org.qi4j.test.entity.AbstractEntityStoreTest;

/**
 * TODO
 */
public class JdbmEntityStoreTest
    extends AbstractEntityStoreTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        super.assemble( module );
        module.addServices( JdbmEntityStoreService.class );

        ModuleAssembly config = module.getLayerAssembly().newModuleAssembly( "config" );
        config.addEntities( JdbmConfiguration.class ).visibleIn( Visibility.layer );
        config.addServices( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class );
    }

    @Test
    public void givenEntityIsNotModifiedWhenUnitOfWorkCompletesThenDontStoreState() throws UnitOfWorkCompletionException
    {
        TestEntity testEntity = null;
        long version = 0;

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }


        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.dereference( testEntity );
            version = spi.getEntityState( testEntity ).version();

            unitOfWork.complete();

        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.dereference( testEntity );
            long newVersion = spi.getEntityState( testEntity ).version();
            assertThat( "version has not changed", newVersion, equalTo( version ) );

            unitOfWork.complete();
        }
    }

    @Test
    public void givenPropertyIsModifiedWhenUnitOfWorkCompletesThenStoreState() throws UnitOfWorkCompletionException
    {
        TestEntity testEntity = null;
        long version = 0;

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }


        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.dereference( testEntity );
            testEntity.name().set( "Rickard" );
            version = spi.getEntityState( testEntity ).version();

            unitOfWork.complete();

        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.dereference( testEntity );
            long newVersion = spi.getEntityState( testEntity ).version();
            assertThat( "version has not changed", newVersion, equalTo( version + 1 ) );

            unitOfWork.complete();
        }
    }

    @Test
    public void givenManyAssociationIsModifiedWhenUnitOfWorkCompletesThenStoreState() throws UnitOfWorkCompletionException
    {
        TestEntity testEntity = null;
        long version = 0;

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }


        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.dereference( testEntity );
            testEntity.manyAssociation().add( testEntity );
            version = spi.getEntityState( testEntity ).version();

            unitOfWork.complete();

        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.dereference( testEntity );
            long newVersion = spi.getEntityState( testEntity ).version();
            assertThat( "version has not changed", newVersion, equalTo( version + 1 ) );

            unitOfWork.complete();
        }
    }

    @Test
    public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
        throws UnitOfWorkCompletionException
    {
        TestEntity testEntity;
        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }

        UnitOfWork unitOfWork1;
        TestEntity testEntity1;
        {
            // Start working with Entity in one UoW
            unitOfWork1 = unitOfWorkFactory.newUnitOfWork();
            testEntity1 = unitOfWork1.dereference( testEntity );
            testEntity1.name().set( "A" );
            testEntity1.unsetName().set( "A" );
        }

        {
            // Start working with same Entity in another UoW, and complete it
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            TestEntity testEntity2 = unitOfWork.dereference( testEntity );
            testEntity2.name().set( "B" );
            unitOfWork.complete();
        }

        {
            // Try to complete first UnitOfWork
            try
            {
                unitOfWork1.complete();
                Assert.fail( "Should have thrown concurrent modification exception" );
            }
            catch( ConcurrentEntityModificationException e )
            {
                e.refreshEntities( unitOfWork1 );

                assertThat( "property name has been refreshed", testEntity1.name().get(), equalTo( "B" ) );

                // Set it again
                testEntity1.name().set( "A" );

                unitOfWork1.complete();
            }
        }

        {
            // Check values
            unitOfWork1 = unitOfWorkFactory.newUnitOfWork();
            testEntity1 = unitOfWork1.dereference( testEntity );
            assertThat( "property name has been set", testEntity1.name().get(), equalTo( "A" ) );
            assertThat( "version is correct", spi.getEntityState( testEntity1 ).version(), equalTo( 2L ) );
            unitOfWork1.discard();
        }
    }

    @Override @After public void tearDown() throws Exception
    {
        super.tearDown();
        boolean deleted = new File( "qi4j.data.db" ).delete();
        deleted = deleted | new File( "qi4j.data.lg" ).delete();
        if( !deleted )
        {
            System.err.println( "Could not delete test data" );
        }
    }
}