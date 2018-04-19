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
package org.apache.polygene.runtime.service;

import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Initializable;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ComplexActivatableTest
    extends AbstractPolygeneTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( SuperType.class ).withActivators( TestActivator.class ).instantiateOnStartup();
    }

    @Test
    public void validateThatApplicationGotAssembled()
    {
        ServiceReference<SuperType> reference = serviceFinder.findService( SuperType.class );
        assertThat( reference.get().sayHello(), equalTo( "Hello, World" ) );
    }

    @Mixins( { DomainType.class, InitializationMixin.class } )
    public interface SuperType
        extends ServiceComposite, Initializable
    {

        String sayHello();

        Property<String> greeting();

        Property<String> recepient();
    }

    public abstract static class DomainType
        implements SuperType
    {

        public String sayHello()
        {
            return greeting().get() + ", " + recepient().get();
        }
    }

    public static class InitializationMixin
        implements Initializable
    {

        @This
        private SuperType me;

        public void initialize()
        {
            me.greeting().set( "Hello" );
        }
    }

    public static class TestActivator
        extends ActivatorAdapter<ServiceReference<SuperType>>
    {

        @Override
        public void afterActivation( ServiceReference<SuperType> activated )
            throws Exception
        {
            activated.get().recepient().set( "World" );
        }
    }
}
