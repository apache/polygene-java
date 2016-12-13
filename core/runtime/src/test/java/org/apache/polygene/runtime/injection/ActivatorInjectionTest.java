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

import org.junit.Test;
import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Layer;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Assert that injections on Activators works.
 */
public class ActivatorInjectionTest
    extends AbstractPolygeneTest
{

    @Mixins( MixinA.class )
    public interface ServiceA
    {
        String what();
    }

    public static class MixinA
        implements ServiceA
    {

        @Override
        public String what()
        {
            return "A";
        }

    }

    @Mixins( MixinB.class )
    public interface ServiceB
    {
        String what();
    }

    public static class MixinB
        implements ServiceB
    {

        @Override
        public String what()
        {
            return "B";
        }

    }

    public static class Foo
    {
        public String bar()
        {
            return "BAZAR";
        }
    }

    public static class ServiceAActivator
        extends ActivatorAdapter<ServiceReference<ServiceA>>
    {
        @Structure
        private Application application;
        @Structure
        private Layer layer;
        @Structure
        private Module module;
        @Service
        private ServiceReference<ServiceB> serviceRefB;
        @Service
        private ServiceB serviceB;

        @Override
        public void afterActivation( ServiceReference<ServiceA> activatee )
            throws Exception
        {
            assertThat( application, notNullValue() );
            assertThat( layer, notNullValue() );
            assertThat( module, notNullValue() );
            assertThat( serviceRefB.isActive(), is( false ) );
            assertThat( serviceB, notNullValue() );
            assertThat( serviceB.what(), equalTo( "B" ) );
        }
    }

    public static class ServiceBActivator
        extends ActivatorAdapter<ServiceReference<ServiceB>>
    {
        @Structure
        private Application application;
        @Structure
        private Layer layer;
        @Structure
        private Module module;
        @Uses
        private Foo foo;

        @Override
        public void afterActivation( ServiceReference<ServiceB> activatee )
            throws Exception
        {
            assertThat( application, notNullValue() );
            assertThat( layer, notNullValue() );
            assertThat( module, notNullValue() );
            assertThat( foo.bar(), equalTo( "BAZAR" ) );
        }
    }

    public static class ModuleActivator
        extends ActivatorAdapter<Module>
    {

        @Override
        public void afterActivation( Module activatee )
            throws Exception
        {
            // No injection support in Structure Activators
            assertThat( activatee, notNullValue() );
        }
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.withActivators( ModuleActivator.class );
        module.objects( Foo.class );
        module.services( ServiceA.class ).withActivators( ServiceAActivator.class ).instantiateOnStartup();
        module.services( ServiceB.class ).withActivators( ServiceBActivator.class ).instantiateOnStartup();
    }

    @Test
    public void test()
    {
        assertThat( serviceFinder.findService( ServiceA.class ).get().what(), equalTo( "A" ) );
        assertThat( serviceFinder.findService( ServiceB.class ).get().what(), equalTo( "B" ) );
    }

}
