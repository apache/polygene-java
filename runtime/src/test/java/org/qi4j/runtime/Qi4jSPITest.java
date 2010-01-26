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

package org.qi4j.runtime;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.AbstractAssociation;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityStateDescriptor;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.*;

/**
 * JAVADOC
 */
public class Qi4jSPITest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.addEntities( TestEntity.class );
    }

    @Test
    public void givenEntityWhenGettingStateThenGetCorrectState()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestEntity testEntity;
        try
        {
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();

            EntityStateHolder state = spi.getState( testEntity );

            validateState( state, spi.getEntityDescriptor( testEntity ) );

            unitOfWork.complete();
        }
        catch( Exception e )
        {
            unitOfWork.discard();
            throw e;
        }

        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            testEntity = uow.get( testEntity );
            validateState( spi.getState( testEntity ), spi.getEntityDescriptor( testEntity ) );
            uow.complete();
        }
        catch( Exception e )
        {
            uow.discard();
            throw e;
        }
    }

    private void validateState( EntityStateHolder state, EntityDescriptor entityDescriptor )
    {
        for( PropertyDescriptor propertyDescriptor : entityDescriptor.state().properties() )
        {
            Property<?> prop = state.getProperty( propertyDescriptor.accessor() );
            assertThat( "Properties could be listed", prop, CoreMatchers.notNullValue() );
        }

        EntityStateDescriptor descriptor = (EntityStateDescriptor) entityDescriptor.state();
        for( AssociationDescriptor associationDescriptor : descriptor.associations() )
        {
            AbstractAssociation assoc = state.getAssociation( associationDescriptor.accessor() );
            assertThat( "Assocs could be listed", assoc, CoreMatchers.notNullValue() );
        }
    }

    public interface TestEntity
        extends EntityComposite
    {
        @Optional
        Property<String> property();

        @Optional
        Association<TestEntity> association();

        ManyAssociation<TestEntity> manyAssociation();
    }
}
