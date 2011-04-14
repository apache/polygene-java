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

package org.qi4j.library.jmx;

import org.junit.Test;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.test.EntityTestAssembler;

/**
 * Start a simple server so that it can be accessed through JMX remotely.
 * Run this with -Dcom.sun.management.jmxremote so that the JVM starts the MBeanServer
 */
public class JMXTest
{
    public static void main( String[] args )
        throws InterruptedException
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
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                new JMXAssembler().assemble( module );

                new EntityTestAssembler().assemble( module );

                module.services( TestService.class ).instantiateOnStartup();
                module.entities( TestConfiguration.class );

                module.values( TestValue.class );

                module.objects( TestObject.class );

                module.services( JMXConnectorService.class ).instantiateOnStartup();
                module.entities( JMXConnectorConfiguration.class );
                module.forMixin( JMXConnectorConfiguration.class ).declareDefaults().port().set( 1099 );
            }
        };

        // This allows user to connect using VisualVM/JConsole
        while(true)
        {
            Thread.sleep(10000);
        }
    }

    @Test
    public void dummy()
    {
    }

    @Mixins( TestService.Mixin.class)
    interface TestService
        extends Configuration<TestConfiguration>, Activatable, ServiceComposite
    {

        class Mixin
            implements Activatable
        {
            @This
            Configuration<TestConfiguration> config;

            public void activate()
                throws Exception
            {
                System.out.println("Activate service:"+config.configuration().stringConfig().get());
            }

            public void passivate()
                throws Exception
            {
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
        Value1,Value2,Value3
    }

    interface TestValue
        extends ValueComposite
    {}

    public static class TestObject
    {
    }
}
