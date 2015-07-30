/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.zest.library.jmx;

import org.junit.Test;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.activation.ActivatorAdapter;
import org.apache.zest.api.activation.Activators;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.configuration.ConfigurationComposite;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;
import org.apache.zest.test.EntityTestAssembler;

/**
 * Start a simple server so that it can be accessed through JMX remotely.
 * Run this with -Dcom.sun.management.jmxremote so that the JVM starts the MBeanServer
 */
public class JMXTest
{

    public static void main( String[] args )
        throws InterruptedException, ActivationException, AssemblyException
    {
        /*
         Logger logger = Logger.getLogger( "" );
         logger.setLevel( Level.FINE );
         Logger.getLogger("sun.rmi").setLevel( Level.WARNING );

         ConsoleHandler consoleHandler = new ConsoleHandler();
         consoleHandler.setLevel( Level.FINE );
         logger.addHandler( consoleHandler );
         */

        SingletonAssembler assembler = new SingletonAssembler()
        {
            // START SNIPPET: assembly

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                // END SNIPPET: assembly
                new EntityTestAssembler().assemble( module );

                module.services( TestService.class, TestService2.class, TestService3.class ).instantiateOnStartup();
                module.entities( TestConfiguration.class );

                module.values( TestValue.class );

                module.objects( TestObject.class );

                // START SNIPPET: assembly
                new JMXAssembler().assemble( module );

                module.services( JMXConnectorService.class ).instantiateOnStartup();
                module.entities( JMXConnectorConfiguration.class );
                module.forMixin( JMXConnectorConfiguration.class ).declareDefaults().port().set( 1099 );
            }
            // END SNIPPET: assembly

        };

        // This allows user to connect using VisualVM/JConsole
        while ( true ) {
            Thread.sleep( 10000 );
        }
    }

    @Test
    public void dummy()
    {
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
    interface TestService
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
    interface TestService2
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
    interface TestService3
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

    interface TestConfiguration
            extends ConfigurationComposite
    {

        @UseDefaults
        Property<String> stringConfig();

        @UseDefaults
        Property<TestEnum> enumConfig();

    }

    enum TestEnum
    {

        Value1, Value2, Value3

    }

    interface TestValue
            extends ValueComposite
    {
    }

    public static class TestObject
    {
    }

}
