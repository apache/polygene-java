/*  Copyright 2008 Jan Kronquist.
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
package org.qi4j.entity.s3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.javaspaces.JavaSpacesEntityStoreService;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

/**
 * JavaSpaces EntityStore test
 */
public class JavaSpacesEntityStoreTest 
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( UuidIdentityGeneratorService.class, JavaSpacesEntityStoreService.class );
        module.addComposites( TestEntity.class );
    }

    @Test
    public void whenNewEntityThenCanFindEntity()
        throws Exception
    {
        try
        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            TestEntity instance = createEntity( unitOfWork );
            unitOfWork.complete();

            // Find entity
            unitOfWork = unitOfWorkFactory.newUnitOfWork();
            instance = unitOfWork.dereference( instance );

            // Check state
            assertThat( "property has correct value", instance.name().get(), equalTo( "Test" ) );
            assertThat( "property has correct value", instance.unsetName().get(), equalTo( null ) );
//            assertThat( "association has correct value", instance.association().get(), equalTo( instance ) );
//            assertThat( "manyAssociation has correct value", instance.manyAssociation().iterator().next(), equalTo( instance ) );
//            assertThat( "listAssociation has correct value", instance.listAssociation().iterator().next(), equalTo( instance ) );
//            assertThat( "setAssociation has correct value", instance.setAssociation().iterator().next(), equalTo( instance ) );
//            assertThat( "setAssociation has correct size", instance.setAssociation().size(), equalTo( 1 ) );
//            assertThat( "listAssociation has correct size", instance.listAssociation().size(), equalTo( 3 ) );

            unitOfWork.discard();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Test
    public void whenRemovedEntityThenCannotFindEntity()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestEntity newInstance = createEntity( unitOfWork );
        String identity = newInstance.identity().get();
        unitOfWork.complete();

        // Remove entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestEntity instance = unitOfWork.dereference( newInstance );
        unitOfWork.remove( instance );
        unitOfWork.complete();

        // Find entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            instance = unitOfWork.find( identity, TestEntity.class );
            fail( "Should not be able to find entity" );
        }
        catch( EntityCompositeNotFoundException e )
        {
            // Ok!
        }
        unitOfWork.discard();
    }

    protected TestEntity createEntity( UnitOfWork unitOfWork )
        throws UnitOfWorkCompletionException
    {
        // Create entity
        CompositeBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );
        TestEntity instance = builder.newInstance();
        String id = instance.identity().get();

        instance.name().set( "Test" );
//        instance.association().set( instance );

//        instance.manyAssociation().add( instance );

//        instance.listAssociation().add( instance );
//        instance.listAssociation().add( instance );
//        instance.listAssociation().add( instance );

//        instance.setAssociation().add( instance );
//        instance.setAssociation().add( instance );
        return instance;
    }

    public interface TestEntity
        extends EntityComposite
    {
        Property<String> name();

        Property<String> unsetName();

//        Association<TestEntity> association();
//
//        Association<TestEntity> unsetAssociation();
//
//        ManyAssociation<TestEntity> manyAssociation();
//
//        ListAssociation<TestEntity> listAssociation();
//
//        SetAssociation<TestEntity> setAssociation();
    }
}