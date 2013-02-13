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

package org.qi4j.api.injection.scope;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

/**
 * Define a field to be a Property
 */
public class StateFieldTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( PersonEntity.class );
    }

    @Test
    public void givenEntityWithFieldPropertiesWhenUpdatedThenReturnCorrect()
        throws Exception
    {
        UnitOfWork unitOfWork = module.newUnitOfWork();
        try
        {
            PersonEntity charles = unitOfWork.newEntity( PersonEntity.class );
            charles.changeName( "Charles" );
            Assert.assertEquals( charles.getName(), "Charles" );

            PersonEntity daniel = unitOfWork.newEntity( PersonEntity.class );
            daniel.changeName( "Daniel" );
            Assert.assertEquals( daniel.getName(), "Daniel" );

            PersonEntity lisa = unitOfWork.newEntity( PersonEntity.class );
            lisa.changeName( "Lisa" );
            Assert.assertEquals( lisa.getName(), "Lisa" );

            charles.befriend( daniel );
            charles.befriend( lisa );
            charles.marry( lisa );

            unitOfWork.complete();

            unitOfWork = module.newUnitOfWork();

            charles = unitOfWork.get( charles );
            daniel = unitOfWork.get( daniel );
            Assert.assertTrue( charles.isFriend( daniel ) );

            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    @Mixins( PersonEntity.Mixin.class )
    public interface PersonEntity
        extends EntityComposite
    {
        void changeName( String newName );

        void marry( PersonEntity entity );

        void befriend( PersonEntity entity );

        boolean isFriend( PersonEntity entity );

        String getName();

        abstract class Mixin
            implements PersonEntity
        {
            @State
            @UseDefaults
            public Property<String> name;

            @State
            @Optional
            public Association<PersonEntity> spouse;

            @State
            public ManyAssociation<PersonEntity> friends;

            @Override
            public void changeName( String newName )
            {
                name.set( newName );
            }

            @Override
            public void marry( PersonEntity entity )
            {
                spouse.set( entity );
            }

            @Override
            public void befriend( PersonEntity entity )
            {
                friends.add( entity );
            }

            @Override
            public String getName()
            {
                return name.get();
            }

            @Override
            public boolean isFriend( PersonEntity entity )
            {
                return friends.contains( entity );
            }
        }
    }
}