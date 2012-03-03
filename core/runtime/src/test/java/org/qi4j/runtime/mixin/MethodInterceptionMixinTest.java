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

package org.qi4j.runtime.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertEquals;

public class MethodInterceptionMixinTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( SomeService.class );
    }

    @Test
    public void whenMixinCallsPublicMethodExpectInvocationStackToBeCalled()
    {
        ServiceReference<SomeService> service = module.findService( SomeService.class );
        Collection<String> result1 = service.get().result();
        assertEquals( "Concern should have been called.", 1, result1.size() );
        assertEquals( "Concern should have been called.", "Concern1", result1.iterator().next() );
    }

    @Concerns( { SomeConcern1.class } )
    @Mixins( { SomeMixin.class } )
    public interface SomeService
        extends Some, ServiceComposite
    {
    }

    public interface Some
    {
        Collection<String> doSome();

        Collection<String> result();
    }

    public static abstract class SomeMixin
        implements Some
    {

        @This
        Some some;

        public Collection<String> doSome()
        {
            return new ArrayList<String>();
        }

        public List<String> result()
        {
            return (List<String>) some.doSome();
        }
    }

    public static abstract class SomeConcern1
        extends ConcernOf<Some>
        implements Some
    {

        public Collection<String> doSome()
        {
            Collection<String> some = next.doSome();
            some.add( "Concern1" );
            return some;
        }
    }
}
