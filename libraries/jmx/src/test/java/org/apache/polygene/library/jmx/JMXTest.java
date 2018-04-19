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
package org.apache.polygene.library.jmx;

import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.activation.Activators;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.configuration.ConfigurationComposite;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.util.JmxFixture;
import org.junit.jupiter.api.Test;

/**
 * Start a simple server so that it can be accessed through JMX remotely.
 * Run this with -Dcom.sun.management.jmxremote so that the JVM starts the MBeanServer
 */
public class JMXTest extends AbstractPolygeneTest
{
    @Override
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );

        module.services( TestService.class, TestService2.class, TestService3.class ).instantiateOnStartup();
        module.entities( TestConfiguration.class );

        module.values( TestValue.class );

        module.objects( TestObject.class );

        // START SNIPPET: assembly
        new JMXAssembler().assemble( module );
        // END SNIPPET: assembly

        // START SNIPPET: connector
        module.services( JMXConnectorService.class ).instantiateOnStartup();
        module.entities( JMXConnectorConfiguration.class );
        module.forMixin( JMXConnectorConfiguration.class ).declareDefaults().port().set( 1099 );
        // END SNIPPET: connector
    }

    public static void main(String[] args )
        throws InterruptedException, ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler(
            moduleAssembly -> new JMXTest().assemble(moduleAssembly)
        );
        // This allows user to connect using VisualVM/JConsole
        while ( true ) {
            Thread.sleep( 10_000 );
        }
    }



    @Test
    public void servicesAndConfiguration()
    {
        JmxFixture jmx = new JmxFixture("Polygene:application=Application,layer=Layer 1,module=Module 1,class=Service,");
        jmx.assertObjectPresent("service=TestService");
        jmx.assertObjectPresent("service=TestService,name=Configuration");
        jmx.assertObjectPresent("service=TestService2");
        jmx.assertObjectPresent("service=TestService2,name=Configuration");
        jmx.assertObjectPresent("service=TestService3");
        jmx.assertObjectPresent("service=TestService3,name=Configuration");
    }

    public interface TestActivation
    {

        void printConfig();

    }

    public static class TestActivator
            extends ActivatorAdapter<ServiceReference<TestActivation>>
    {

        @Override
        public void afterActivation( ServiceReference<TestActivation> activated )
                throws Exception
        {
            activated.get().printConfig();
        }

    }

    @Mixins( TestService.Mixin.class )
    @Activators( TestActivator.class )
    public interface TestService
            extends TestActivation, ServiceComposite
    {

        class Mixin
                implements TestActivation
        {

            @This
            Configuration<TestConfiguration> config;

            public void printConfig()
            {
                System.out.println( "Activate service:" + config.get().stringConfig().get() );
            }

        }

    }

    @Mixins( TestService2.Mixin.class )
    @Activators( TestActivator.class )
    public interface TestService2
            extends TestActivation, ServiceComposite
    {

        class Mixin
                implements TestActivation
        {

            @This
            Configuration<TestConfiguration> config;

            public void printConfig()
            {
                System.out.println( "Activate service:" + config.get().stringConfig().get() );
            }

        }

    }

    @Mixins( TestService3.Mixin.class )
    @Activators( TestActivator.class )
    public interface TestService3
            extends TestActivation, ServiceComposite
    {

        class Mixin
                implements TestActivation
        {

            @This
            Configuration<TestConfiguration> config;

            public void printConfig()
            {
                System.out.println( "Activate service:" + config.get().stringConfig().get() );
            }

        }

    }

    public interface TestConfiguration
            extends ConfigurationComposite
    {

        @UseDefaults
        Property<String> stringConfig();

        @UseDefaults
        Property<TestEnum> enumConfig();

    }

    public enum TestEnum
    {

        Value1, Value2, Value3

    }

    public interface TestValue
            extends ValueComposite
    {
    }

    public static class TestObject
    {
    }

}
