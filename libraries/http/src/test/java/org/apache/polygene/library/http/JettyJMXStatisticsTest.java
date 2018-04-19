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
package org.apache.polygene.library.http;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.jmx.JMXAssembler;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.util.FreePortFinder;
import org.junit.jupiter.api.Test;

import static org.apache.polygene.library.http.Servlets.addServlets;
import static org.apache.polygene.library.http.Servlets.serve;

public class JettyJMXStatisticsTest
    extends AbstractPolygeneTest
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
        config.port().set( FreePortFinder.findFreePortOnLoopback() );
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
