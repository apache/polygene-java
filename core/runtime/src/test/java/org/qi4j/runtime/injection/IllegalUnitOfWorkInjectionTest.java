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
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.fail;

public class IllegalUnitOfWorkInjectionTest
    extends AbstractQi4jTest
{
    private boolean failed = false;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TrialTransient.class );
        new EntityTestAssembler().assemble( module );
    }

    @Override
    protected void assemblyException( AssemblyException exception )
        throws AssemblyException
    {
        failed = true;
    }

    @Test
    public void givenTransientCompositeWhenInjectingUnitOfWorkThenExpectAnInjectionException()
        throws Exception
    {
        if( !failed )
        {
            fail( "Transients should not be allowed to have @State UnitOfWork injections." );
        }
    }

    interface Trial
    {
        void doSomething();

        String usecaseName();
    }

    @Mixins( TrialMixin.class )
    interface TrialTransient
        extends Trial, TransientComposite
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

