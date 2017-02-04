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

package org.apache.polygene.runtime.injection;

import org.apache.polygene.api.identity.StringIdentity;
import org.junit.Test;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.injection.scope.State;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.usecase.Usecase;
import org.apache.polygene.api.usecase.UsecaseBuilder;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;

public class UnitOfWorkInjectionTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( TrialEntity.class );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void givenEntityInOneUnitOfWorkWhenCurrentUnitOfWorkHasChangedThenUnitOfWorkInjectionInEntityPointsToCorrectUow()
        throws Exception
    {
        Usecase usecase = UsecaseBuilder.newUsecase( "usecase1" );
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( usecase );
        try
        {
            Trial trial = uow.newEntity( Trial.class, new StringIdentity( "123" ) );
            trial.doSomething();
            uow.complete();
            uow = unitOfWorkFactory.newUnitOfWork( usecase );
            usecase = UsecaseBuilder.newUsecase( "usecase2" );
            UnitOfWork uow2 = unitOfWorkFactory.newUnitOfWork( usecase );
            trial = uow.get( trial );
            trial.doSomething();
            assertEquals( "123", ( (EntityComposite) trial ).identity().get().toString() );
            assertEquals( "usecase1", trial.usecaseName() );
            uow2.discard();
        }
        catch( Throwable ex )
        {
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                while( unitOfWorkFactory.isUnitOfWorkActive() )
                {
                    uow = unitOfWorkFactory.currentUnitOfWork();
                    uow.discard();
                }
            }
            catch( IllegalStateException e )
            {
                // Continue
            }
        }
    }

    public interface Trial
    {
        void doSomething();

        String usecaseName();
    }

    @Mixins( TrialMixin.class )
    public interface TrialEntity
        extends Trial, EntityComposite
    {
    }

    public static class TrialMixin
        implements Trial
    {
        @State
        private UnitOfWork uow;

        private String uowIdentity;

        public void doSomething()
        {
            uowIdentity = uow.usecase().name();
        }

        public String usecaseName()
        {
            return uowIdentity;
        }
    }
}

