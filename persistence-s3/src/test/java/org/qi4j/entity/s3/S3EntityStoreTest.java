/*  Copyright 2008 Rickard …berg.
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

import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Mixins;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.library.framework.entity.AssociationMixin;
import org.qi4j.library.framework.entity.PropertyMixin;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.UuidIdentityGeneratorComposite;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Amazon S3 EntityStore test
 */
public class S3EntityStoreTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( S3EntityStoreComposite.class, UuidIdentityGeneratorComposite.class );
        module.addComposites( TestComposite.class );
    }

    @Test
    public void dummyTest()
    {
        // All tests are disabled since by default the S3 store doesn't work due to missing account keys!
    }

    //    @Test
    public void whenNewEntityThenFindEntity()
        throws Exception
    {
        String id = createEntity( null );
        UnitOfWork unitOfWork;
        TestComposite instance;

        // Find entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        instance = unitOfWork.find( id, TestComposite.class );
        org.junit.Assert.assertThat( "property has correct value", instance.name().get(), org.hamcrest.CoreMatchers.equalTo( "Rickard" ) );
        unitOfWork.discard();
    }

    //    @Test
    public void whenRemovedEntityThenCannotFindEntity()
        throws Exception
    {
        String id = createEntity( null );

        // Remove entity
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestComposite instance = unitOfWork.find( id, TestComposite.class );
        unitOfWork.remove( instance );
        unitOfWork.complete();

        // Find entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            instance = unitOfWork.find( id, TestComposite.class );
            org.junit.Assert.fail( "Should not be able to find entity" );
        }
        catch( EntityCompositeNotFoundException e )
        {
            // Ok!
        }
        unitOfWork.discard();
    }

    private String createEntity( String id )
        throws UnitOfWorkCompletionException
    {
        // Create entity
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        CompositeBuilder<TestComposite> builder = unitOfWork.newEntityBuilder( id, TestComposite.class );
        builder.propertiesOfComposite().name().set( "Rickard" );
        TestComposite instance = builder.newInstance();
        id = instance.identity().get();
        unitOfWork.complete();
        return id;
    }

    @Mixins( { PropertyMixin.class, AssociationMixin.class } )
    public interface TestComposite
        extends EntityComposite
    {
        Property<String> name();
    }
}