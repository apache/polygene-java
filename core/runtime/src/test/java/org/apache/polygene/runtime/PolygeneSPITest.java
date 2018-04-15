/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.runtime;

import org.apache.polygene.api.association.AbstractAssociation;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.AssociationStateDescriptor;
import org.apache.polygene.api.association.AssociationStateHolder;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * JAVADOC
 */
public class PolygeneSPITest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( TestEntity.class, TestEntity2.class );
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

            AssociationStateHolder state = spi.stateOf( testEntity );

            validateState( state, spi.entityDescriptorFor( testEntity ) );

            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }

        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            testEntity = uow.get( testEntity );
            validateState( spi.stateOf( testEntity ), spi.entityDescriptorFor( testEntity ) );
            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    private void validateState( AssociationStateHolder state, EntityDescriptor entityDescriptor )
    {
        entityDescriptor.state().properties().forEach( propertyDescriptor -> {
            Property<?> prop = state.propertyFor( propertyDescriptor.accessor() );
            assertThat( "Properties could be listed", prop, CoreMatchers.notNullValue() );
        } );

        AssociationStateDescriptor descriptor = entityDescriptor.state();
        descriptor.associations().forEach( associationDescriptor -> {
            AbstractAssociation assoc = state.associationFor( associationDescriptor.accessor() );
            assertThat( "Assocs could be listed", assoc, CoreMatchers.notNullValue() );
        } );
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

    public interface TestEntity2
        extends EntityComposite
    {
        @Optional
        Property<String> property();

        @Optional
        Association<TestEntity> association();

        ManyAssociation<TestEntity> manyAssociation();
    }
}
