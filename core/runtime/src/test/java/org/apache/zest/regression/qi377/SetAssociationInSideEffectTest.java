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
package org.apache.zest.regression.qi377;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import org.junit.Test;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.common.AppliesTo;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.sideeffect.GenericSideEffect;
import org.apache.zest.api.sideeffect.SideEffects;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.theInstance;
import static org.junit.Assert.assertThat;

public class SetAssociationInSideEffectTest
    extends AbstractZestTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );

        module.entities( Pianist.class, Steinway.class );
    }

    @Test
    public void whenSettingAnAssociationInASideEffectExpectItToWork()
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "Purchase Steinway" ) ) )
        {
            Pianist chris = uow.newEntity( Pianist.class, "Chris" );
            Steinway modelD = uow.newEntity( Steinway.class, "ModelD-274" );

            assertThat( modelD.owner().get(), is( nullValue() ) );

            chris.purchase( modelD );

            assertThat( modelD.owner().get(), is( theInstance( (Owner) chris ) ) );
        }
    }

    @Mixins( PianistMixin.class )
    @SideEffects( ChangeOwnerSideEffect.class )
    public interface Pianist
        extends Owner, EntityComposite
    {
        @Optional
        Association<Steinway> steinway();

        @ChangesOwner
        void purchase( Steinway piano );
    }

    public static abstract class PianistMixin
        implements Pianist
    {
        @Override
        public void purchase( Steinway piano )
        {
            steinway().set( piano );
        }
    }

    public interface Steinway
        extends Ownable, EntityComposite
    {
    }

    public interface Owner
    {
    }

    public interface Ownable
    {
        @Optional
        Association<Owner> owner();
    }

    @AppliesTo( ChangesOwner.class )
    public static class ChangeOwnerSideEffect
        extends GenericSideEffect
    {
        @This
        Owner owner;

        @Override
        protected void invoke( Method method, Object[] args )
            throws Throwable
        {
            Ownable ownable = (Ownable) args[ 0];
            ownable.owner().set( owner );
        }
    }

    @Retention( RetentionPolicy.RUNTIME )
    public @interface ChangesOwner
    {
    }

}
