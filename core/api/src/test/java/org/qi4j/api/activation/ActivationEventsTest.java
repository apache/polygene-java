/*
 * Copyright (c) 2011, Rickard Öberg.
 * Copyright (c) 2012, Niclas Hedhman.
 * Copyright (c) 2012, Paul Merlin.
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
package org.qi4j.api.activation;

import org.qi4j.api.activation.ActivationEvent;
import org.qi4j.api.activation.ActivationEventListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.qi4j.api.activation.ActivationEvent.EventType;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

import static junit.framework.Assert.*;
import static org.qi4j.api.activation.ActivationEvent.EventType.*;

public class ActivationEventsTest
{

    public static interface TestService
    {
        void test();
    }

    public static class TestServiceInstance
            implements TestService
    {

        public void test()
        {
        }

    }

    @Mixins( TestServiceInstance.class )
    public static interface TestServiceComposite
        extends TestService, ServiceComposite
    {
    }

    @Test
    public void testSingleModuleSingleService()
        throws Exception
    {
        final List<ActivationEvent> events = new ArrayList<ActivationEvent>();

        new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.services( TestServiceComposite.class ).instantiateOnStartup();
            }

            @Override
            protected void beforeActivation( Application application )
            {
                application.registerActivationEventListener( new EventsRecorder( events ) );
            }


        }.application().passivate();

        Iterator<ActivationEvent> it = events.iterator();

        // Activation
        assertEvent( it.next(), ACTIVATING, "Application" );
        assertEvent( it.next(), ACTIVATING, "Layer" );
        assertEvent( it.next(), ACTIVATING, "Module" );
        assertEvent( it.next(), ACTIVATING, "TestService" );
        assertEvent( it.next(), ACTIVATED, "TestService" );
        assertEvent( it.next(), ACTIVATED, "Module" );
        assertEvent( it.next(), ACTIVATED, "Layer" );
        assertEvent( it.next(), ACTIVATED, "Application" );

        // Passivation
        assertEvent( it.next(), PASSIVATING, "Application" );
        assertEvent( it.next(), PASSIVATING, "Layer" );
        assertEvent( it.next(), PASSIVATING, "Module" );
        assertEvent( it.next(), PASSIVATING, "TestService" );
        assertEvent( it.next(), PASSIVATED, "TestService" );
        assertEvent( it.next(), PASSIVATED, "Module" );
        assertEvent( it.next(), PASSIVATED, "Layer" );
        assertEvent( it.next(), PASSIVATED, "Application" );

        assertFalse( it.hasNext() );
    }

    @Test
    public void testSingleModuleSingleImportedService()
            throws Exception
    {
        final List<ActivationEvent> events = new ArrayList<ActivationEvent>();

        new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.importedServices( TestService.class ).
                        setMetaInfo( new TestServiceInstance() ).
                        importOnStartup();
            }

            @Override
            protected void beforeActivation( Application application )
            {
                application.registerActivationEventListener( new EventsRecorder( events ) );
            }


        }.application().passivate();

        Iterator<ActivationEvent> it = events.iterator();

        // Activation
        assertEvent( it.next(), ACTIVATING, "Application" );
        assertEvent( it.next(), ACTIVATING, "Layer" );
        assertEvent( it.next(), ACTIVATING, "Module" );
        assertEvent( it.next(), ACTIVATING, "TestService" );
        assertEvent( it.next(), ACTIVATED, "TestService" );
        assertEvent( it.next(), ACTIVATED, "Module" );
        assertEvent( it.next(), ACTIVATED, "Layer" );
        assertEvent( it.next(), ACTIVATED, "Application" );

        // Passivation
        assertEvent( it.next(), PASSIVATING, "Application" );
        assertEvent( it.next(), PASSIVATING, "Layer" );
        assertEvent( it.next(), PASSIVATING, "Module" );
        assertEvent( it.next(), PASSIVATING, "TestService" );
        assertEvent( it.next(), PASSIVATED, "TestService" );
        assertEvent( it.next(), PASSIVATED, "Module" );
        assertEvent( it.next(), PASSIVATED, "Layer" );
        assertEvent( it.next(), PASSIVATED, "Application" );

        assertFalse( it.hasNext() );
    }

    @Test
    public void testSingleModuleSingleLazyService()
            throws Exception
    {
        final List<ActivationEvent> events = new ArrayList<ActivationEvent>();

        SingletonAssembler assembler = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.services( TestServiceComposite.class );
            }

            @Override
            protected void beforeActivation( Application application )
            {
                application.registerActivationEventListener( new EventsRecorder( events ) );
            }

        };
        Application application = assembler.application();
        application.passivate();

        Iterator<ActivationEvent> it = events.iterator();

        // Activation
        assertEvent( it.next(), ACTIVATING, "Application" );
        assertEvent( it.next(), ACTIVATING, "Layer" );
        assertEvent( it.next(), ACTIVATING, "Module" );
        // Lazy Service NOT activated
        assertEvent( it.next(), ACTIVATED, "Module" );
        assertEvent( it.next(), ACTIVATED, "Layer" );
        assertEvent( it.next(), ACTIVATED, "Application" );

        // Passivation
        assertEvent( it.next(), PASSIVATING, "Application" );
        assertEvent( it.next(), PASSIVATING, "Layer" );
        assertEvent( it.next(), PASSIVATING, "Module" );
        // Lazy Service NOT passivated
        assertEvent( it.next(), PASSIVATED, "Module" );
        assertEvent( it.next(), PASSIVATED, "Layer" );
        assertEvent( it.next(), PASSIVATED, "Application" );

        assertFalse( it.hasNext() );

        events.clear();
        application.activate();
        Module module = assembler.module();
        module.findService( TestService.class ).get().test();
        application.passivate();

        for( ActivationEvent event : events ) {
            System.out.println( event );
        }

        it = events.iterator();

        // Activation
        assertEvent( it.next(), ACTIVATING, "Application" );
        assertEvent( it.next(), ACTIVATING, "Layer" );
        assertEvent( it.next(), ACTIVATING, "Module" );
        assertEvent( it.next(), ACTIVATED, "Module" );
        assertEvent( it.next(), ACTIVATED, "Layer" );
        assertEvent( it.next(), ACTIVATED, "Application" );

        // Lazy Service Activation
        assertEvent( it.next(), ACTIVATING, "TestService" );
        assertEvent( it.next(), ACTIVATED, "TestService" );

        // Passivation
        assertEvent( it.next(), PASSIVATING, "Application" );
        assertEvent( it.next(), PASSIVATING, "Layer" );
        assertEvent( it.next(), PASSIVATING, "Module" );
        assertEvent( it.next(), PASSIVATING, "TestService" );
        assertEvent( it.next(), PASSIVATED, "TestService" );
        assertEvent( it.next(), PASSIVATED, "Module" );
        assertEvent( it.next(), PASSIVATED, "Layer" );
        assertEvent( it.next(), PASSIVATED, "Application" );

        assertFalse( it.hasNext() );
    }

    private static class EventsRecorder
            implements ActivationEventListener
    {

        private final List<ActivationEvent> events;

        private EventsRecorder( List<ActivationEvent> events )
        {
            this.events = events;
        }

        public void onEvent( ActivationEvent event )
        {
            events.add( event );
        }

    }

    // WARN This assertion depends on ApplicationInstance, LayerInstance, ModuleInstance and ServiceReferenceInstance toString() method.
    private static void assertEvent( ActivationEvent event, EventType expectedType, String expected )
    {
        boolean wrongEvent = expectedType != event.type();
        boolean wrongMessage = ! event.message().contains( expected );
        if( wrongEvent || wrongMessage )
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Event (").append( event ).append( ") has");
            if( wrongEvent )
            {
                sb.append( " wrong type (expected:'" ).append( expectedType ).
                        append( "' but was:'" ).append( event.type() ).append( "')" );
                if( wrongMessage )
                {
                    sb.append( ";" );
                }
            }
            if( wrongMessage )
            {
                sb.append( " wrong message (expected:'" ).append( expected ).
                        append( "' but was:'" ).append( event.message() ).append( "')" );
            }
            fail( sb.toString() );
        }
    }

}
