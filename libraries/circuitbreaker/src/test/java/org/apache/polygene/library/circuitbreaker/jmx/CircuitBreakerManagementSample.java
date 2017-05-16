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
package org.apache.polygene.library.circuitbreaker.jmx;

import java.util.Random;
import javax.management.MBeanServer;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.apache.polygene.library.circuitbreaker.CircuitBreaker;
import org.apache.polygene.library.circuitbreaker.CircuitBreakers;
import org.apache.polygene.library.circuitbreaker.service.ServiceCircuitBreaker;
import org.apache.polygene.library.jmx.MBeanServerImporter;

/**
 * Run this as a program and connect with VisualVM. That way you can monitor changes in attributes, notifications, and
 * execute operations on the CircuitBreaker through JMX.
 */
public class CircuitBreakerManagementSample
{

    public static void main( String[] args )
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {

            @Override
            // START SNIPPET: jmx
            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                // END SNIPPET: jmx
                CircuitBreaker cb = new CircuitBreaker( 3, 250, CircuitBreakers.in( IllegalArgumentException.class ) );

                module.importedServices( TestService.class ).setMetaInfo( new TestService( cb ) );

                // START SNIPPET: jmx
                // JMX Library
                module.importedServices( MBeanServer.class ).
                    importedBy( MBeanServerImporter.class );
                // CircuitBreakers in JMX
                module.services( CircuitBreakerManagement.class ).
                    instantiateOnStartup();
            }
            // END SNIPPET: jmx

        };

        TestService service = assembler.module().findService( TestService.class ).get();

        int interval = 1; // Seconds
        System.out.println( "CircuitBreaker JMX Support sample is now started." );
        System.out.println();
        System.out.println( "A Service that randomly output some text or fail is called through a CircuitBreaker every " + interval + " seconds." );
        System.out.println( "In a few interval the CircuitBreaker will be turned off." );
        System.out.println( "Connect with a MBean browser (eg. VisualVM + MBean plugin) to use the turnOn operation on the CircuitBreakers." );
        System.out.println();
        System.out.println( "Hit Ctrl-C to stop." );
        System.out.println();

        while ( true ) {
            try {
                Thread.sleep( interval * 1000 );
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }

            service.helloWorld();
        }
    }

    public static class TestService
            implements ServiceCircuitBreaker
    {

        CircuitBreaker cb;

        Random random = new Random();

        public TestService( CircuitBreaker cb )
        {
            this.cb = cb;
        }

        @Override
        public CircuitBreaker circuitBreaker()
        {
            return cb;
        }

        public void helloWorld()
        {
            if ( random.nextDouble() > 0.3 ) {
                cb.throwable( new Throwable( "Failed" ) );
            } else {
                cb.success();
            }
        }

    }

}
