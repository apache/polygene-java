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
package org.apache.polygene.api.activation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Layer;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.builder.ApplicationBuilder;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class PassivationExceptionTest
{
    private static String stack( Exception ex )
    {
        StringWriter writer = new StringWriter();
        ex.printStackTrace( new PrintWriter( writer ) );
        return writer.toString();
    }

    @Test
    public void testEmptyPassivationException()
    {
        PassivationException empty = new PassivationException( emptyList() );
        assertThat( empty.getMessage(), containsString( "has 0 cause" ) );
    }

    @Test
    public void testSinglePassivationException()
    {
        PassivationException single = new PassivationException( singletonList( new Exception( "single" ) ) );
        String stack = stack( single );
        assertThat( single.getMessage(), containsString( "has 1 cause" ) );
        assertThat( stack, containsString( "Suppressed: java.lang.Exception: single" ) );
    }

    @Test
    public void testMultiplePassivationException()
    {
        PassivationException multi = new PassivationException( Arrays.asList( new Exception( "one" ),
                                                                              new Exception( "two" ),
                                                                              new Exception( "three" ) ) );
        String stack = stack( multi );
        assertThat( multi.getMessage(), containsString( "has 3 cause(s)" ) );
        assertThat( stack, containsString( "Suppressed: java.lang.Exception: one" ) );
        assertThat( stack, containsString( "Suppressed: java.lang.Exception: two" ) );
        assertThat( stack, containsString( "Suppressed: java.lang.Exception: three" ) );
    }

    @Test
    public void testPassivationExceptionsAccrossStructure()
        throws AssemblyException, ActivationException
    {
        ApplicationBuilder appBuilder = new ApplicationBuilder( "TestApplication" );
        appBuilder.withLayer( "Layer 1" ).withModule( "Module A" ).withAssembler(
            module -> module.services( TestService.class )
                            .identifiedBy( "TestService_Module.A" )
                            .withActivators( FailBeforePassivationServiceActivator.class )
                            .instantiateOnStartup() );
        appBuilder.withLayer( "Layer 2" ).withModule( "Module B" ).withAssembler(
            module -> module.services( TestService.class )
                            .identifiedBy( "TestService_Module.B" )
                            .withActivators( FailAfterPassivationServiceActivator.class )
                            .instantiateOnStartup() );
        appBuilder.registerActivationEventListener( new TestActivationEventListener() );

        Application app = appBuilder.newApplication();

        try
        {
            Module moduleA = app.findModule( "Layer 1", "Module A" );
            TestService service = moduleA.findService( TestService.class ).get();
            assertThat( service.hello(), equalTo( "Hello Polygene!" ) );
        }
        finally
        {
            try
            {
                app.passivate();
                fail( "No PassivationException" );
            }
            catch( PassivationException ex )
            {
                ex.printStackTrace();
                String stack = stack( ex );
                assertThat( ex.getMessage(), containsString( "has 12 cause(s)" ) );
                assertThat( stack, containsString( "EVENT: FAIL BEFORE PASSIVATION for TestApplication" ) );
                assertThat( stack, containsString( "EVENT: FAIL BEFORE PASSIVATION for Layer 2" ) );
                assertThat( stack, containsString( "EVENT: FAIL BEFORE PASSIVATION for Module B" ) );
                assertThat(
                    stack,
                    containsString(
                        "ACTIVATOR: FAIL AFTER PASSIVATION for TestService_Module.B(active=false,module='Module B')" ) );
                assertThat( stack, containsString( "EVENT: FAIL AFTER PASSIVATION for Module B" ) );
                assertThat( stack, containsString( "EVENT: FAIL AFTER PASSIVATION for Layer 2" ) );
                assertThat( stack, containsString( "EVENT: FAIL BEFORE PASSIVATION for Layer 1" ) );
                assertThat( stack, containsString( "EVENT: FAIL BEFORE PASSIVATION for Module A" ) );
                assertThat(
                    stack,
                    containsString(
                        "ACTIVATOR: FAIL BEFORE PASSIVATION for TestService_Module.A(active=true,module='Module A')" ) );
                assertThat( stack, containsString( "EVENT: FAIL AFTER PASSIVATION for Module A" ) );
                assertThat( stack, containsString( "EVENT: FAIL AFTER PASSIVATION for Layer 1" ) );
                assertThat( stack, containsString( "EVENT: FAIL AFTER PASSIVATION for TestApplication" ) );
            }
        }
    }

    @Mixins( TestService.Mixin.class )
    public interface TestService
    {
        String hello();

        class Mixin
            implements TestService
        {
            @Structure
            private Module module;

            @Override
            public String hello()
            {
                module.name();
                return "Hello Polygene!";
            }
        }
    }

    public static class FailBeforePassivationServiceActivator
        extends ActivatorAdapter<ServiceReference<TestService>>
    {
        @Override
        public void beforePassivation( ServiceReference<TestService> passivated )
            throws Exception
        {
            throw new Exception( "ACTIVATOR: FAIL BEFORE PASSIVATION for " + passivated );
        }
    }

    public static class FailAfterPassivationServiceActivator
        extends ActivatorAdapter<ServiceReference<TestService>>
    {
        @Override
        public void afterPassivation( ServiceReference<TestService> passivated )
            throws Exception
        {
            throw new Exception( "ACTIVATOR: FAIL AFTER PASSIVATION for " + passivated );
        }
    }

    public static class TestActivationEventListener
        implements ActivationEventListener
    {
        @Override
        public void onEvent( ActivationEvent event )
            throws Exception
        {
            if( !( event.source() instanceof Application )
                && !( event.source() instanceof Layer )
                && !( event.source() instanceof Module ) )
            {
                return;
            }
            switch( event.type() )
            {
                case PASSIVATING:
                    throw new Exception( "EVENT: FAIL BEFORE PASSIVATION for " + event.source() );
                case PASSIVATED:
                    throw new Exception( "EVENT: FAIL AFTER PASSIVATION for " + event.source() );
            }
        }
    }
}
