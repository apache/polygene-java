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

package org.apache.polygene.api.injection.scope;

import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Define a field to be a Property
 */
public class StateFieldTest
    extends AbstractPolygeneTest
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
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity charles = unitOfWork.newEntity( PersonEntity.class );
            charles.changeName( "Charles" );
            assertThat( "Charles", equalTo( charles.getName() ) );

            PersonEntity daniel = unitOfWork.newEntity( PersonEntity.class );
            daniel.changeName( "Daniel" );
            assertThat( "Daniel", equalTo( daniel.getName() ) );

            PersonEntity lisa = unitOfWork.newEntity( PersonEntity.class );
            lisa.changeName( "Lisa" );
            assertThat( "Lisa", equalTo( lisa.getName() ) );

            charles.befriend( daniel );
            charles.befriend( lisa );
            charles.marry( lisa );

            unitOfWork.complete();

            unitOfWork = unitOfWorkFactory.newUnitOfWork();

            charles = unitOfWork.get( charles );
            daniel = unitOfWork.get( daniel );
            assertThat( charles.isFriend( daniel ), is( true ) );

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