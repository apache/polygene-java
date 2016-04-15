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

package org.apache.zest.runtime.injection;

import org.junit.Test;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.State;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;

public class UnitOfWorkInjectionTest
    extends AbstractZestTest
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
        UnitOfWork uow = uowf.newUnitOfWork( usecase );
        try
        {
            Trial trial = uow.newEntity( Trial.class, "123" );
            trial.doSomething();
            uow.complete();
            uow = uowf.newUnitOfWork( usecase );
            usecase = UsecaseBuilder.newUsecase( "usecase2" );
            UnitOfWork uow2 = uowf.newUnitOfWork( usecase );
            trial = uow.get( trial );
            trial.doSomething();
            assertEquals( "123", ( (EntityComposite) trial ).identity().get() );
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
                while( uowf.isUnitOfWorkActive() )
                {
                    uow = uowf.currentUnitOfWork();
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

