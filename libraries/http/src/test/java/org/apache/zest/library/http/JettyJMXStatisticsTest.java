/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
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
package org.apache.zest.library.http;

import org.junit.Test;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.jmx.JMXAssembler;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.apache.zest.library.http.Servlets.addServlets;
import static org.apache.zest.library.http.Servlets.serve;

public class JettyJMXStatisticsTest
    extends AbstractZestTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ModuleAssembly configModule = module;
        new EntityTestAssembler().assemble( configModule );
        // START SNIPPET: jmx
        new JettyServiceAssembler().withConfig( configModule, Visibility.layer ).assemble( module );
        new JMXAssembler().assemble( module ); // Assemble both JettyService and JMX

        JettyConfiguration config = configModule.forMixin( JettyConfiguration.class ).declareDefaults();
        config.hostName().set( "127.0.0.1" );
        config.port().set( 8441 );
        config.statistics().set( Boolean.TRUE ); // Set statistics default to TRUE in configuration

        // Hello world servlet related assembly
        addServlets( serve( "/hello" ).with( HelloWorldServletService.class ) ).to( module );
        // END SNIPPET: jmx
    }

    /**
     * Run this test with -Djmxtest make it to not return so you can connect to the JVM using a JMX client.
     */
    @Test
    public void dummy()
        throws InterruptedException
    {
        if( !"false".equals( System.getProperty( "jmxtest", "false" ) ) )
        {
            Thread.sleep( Long.MAX_VALUE );
        }
    }
}
