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

package org.apache.polygene.runtime.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.apache.polygene.api.concern.ConcernOf;
import org.apache.polygene.api.concern.Concerns;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;

import static org.junit.Assert.assertEquals;

public class MethodInterceptionMixinTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( SomeService.class );
    }

    @Test
    public void whenMixinCallsPublicMethodExpectInvocationStackToBeCalled()
    {
        ServiceReference<SomeService> service = serviceFinder.findService( SomeService.class );
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
