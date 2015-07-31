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

package org.qi4j.runtime.entity.associations;

import org.junit.Test;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

/**
 * Test that associations can be marked as @Immutable
 */
public class ImmutableAssociationTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( PersonEntity.class );
    }

    @Test
    public void givenEntityWithImmutableAssociationWhenBuildingThenNoException()
        throws Exception
    {
        UnitOfWork unitOfWork = module.newUnitOfWork();
        try
        {
            PersonEntity father = unitOfWork.newEntity( PersonEntity.class );

            EntityBuilder<PersonEntity> builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity instance = builder.instance();
            instance.father().set( father );
            PersonEntity child = builder.newInstance();
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    @Test( expected = IllegalStateException.class )
    public void givenEntityWithImmutableAssociationWhenChangingValueThenThrowException()
        throws Exception
    {
        UnitOfWork unitOfWork = module.newUnitOfWork();
        try
        {
            EntityBuilder<PersonEntity> builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity father = builder.instance();
            father = builder.newInstance();

            builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity child = builder.instance();
            child = builder.newInstance();

            child.father().set( father );

            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    @Test
    public void givenEntityWithImmutableManyAssociationWhenBuildingThenNoException()
        throws Exception
    {
        UnitOfWork unitOfWork = module.newUnitOfWork();
        try
        {
            EntityBuilder<PersonEntity> builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity person1 = builder.instance();
            person1 = builder.newInstance();

            builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity person2 = builder.instance();
            person2.colleagues().add( 0, person1 );
            person2 = builder.newInstance();
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    @Test( expected = IllegalStateException.class )
    public void givenEntityWithImmutableManyAssociationWhenChangingValueThenThrowException()
        throws Exception
    {
        UnitOfWork unitOfWork = module.newUnitOfWork();
        try
        {
            EntityBuilder<PersonEntity> builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity person1 = builder.instance();
            person1 = builder.newInstance();

            builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity person2 = builder.instance();
            person2 = builder.newInstance();

            person1.colleagues().add( 0, person2 );

            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    interface PersonEntity
        extends EntityComposite
    {
        @Optional
        @Immutable
        Association<PersonEntity> father();

        @Immutable
        ManyAssociation<PersonEntity> children();

        @Immutable
        ManyAssociation<PersonEntity> friends();

        @Immutable
        ManyAssociation<PersonEntity> colleagues();
    }
}
