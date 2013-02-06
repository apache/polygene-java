/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.runtime.injection;

import org.junit.Test;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;

public class UnitOfWorkInjectionTest
    extends AbstractQi4jTest
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
        UnitOfWork uow = module.newUnitOfWork( usecase );
        try
        {
            Trial trial = uow.newEntity( Trial.class, "123" );
            trial.doSomething();
            uow.complete();
            uow = module.newUnitOfWork( usecase );
            usecase = UsecaseBuilder.newUsecase( "usecase2" );
            UnitOfWork uow2 = module.newUnitOfWork( usecase );
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
                while( module.isUnitOfWorkActive() )
                {
                    uow = module.currentUnitOfWork();
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

