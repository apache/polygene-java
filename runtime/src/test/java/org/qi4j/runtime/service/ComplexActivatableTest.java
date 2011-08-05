/*
 * Copyright (c) 2010, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.runtime.service;

import org.junit.Test;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertEquals;

public class ComplexActivatableTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( SuperType.class ).instantiateOnStartup();
    }

    @Test
    public void validateThatApplicationGotAssembled()
    {
        ServiceReference<SuperType> reference = module.findService( SuperType.class );
        assertEquals( "Hello, World", reference.get().sayHello() );
    }

    @Mixins( { DomainType.class, ActivationMixin.class } )
    public interface SuperType
        extends Activatable, ServiceComposite
    {
        String sayHello();

        Property<String> greeting();

        Property<String> recepient();
    }

    public abstract static class DomainType
        implements SuperType, Activatable
    {
        public String sayHello()
        {
            return greeting().get() + ", " + recepient().get();
        }
    }

    public static class ActivationMixin
        implements Activatable, Initializable
    {
        @This
        private SuperType me;

        public void activate()
            throws Exception
        {
            me.greeting().set( "Hello" );
        }

        public void passivate()
            throws Exception
        {
        }

        public void initialize()
            throws InitializationException
        {
            me.recepient().set( "World" );
        }
    }
}
