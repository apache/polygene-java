/*
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
package org.qi4j.runtime.activation;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

import static org.junit.Assert.*;
import static org.qi4j.runtime.activation.ActivatorOrderTestSupport.*;

public class ServiceActivatorOrderTest
{

    //
    // ActivationStepsRecorder --------------------------------------------
    //
    public static final ActivationStepsRecorder RECORDER = new ActivationStepsRecorderInstance();

    @Before
    public void beforeEachTest()
    {
        RECORDER.reset();
    }

    //
    // FooBarService ------------------------------------------------------
    //
    @Mixins( FooBarInstance.class )
    public static interface FooBarService
            extends FooBar, ServiceComposite
    {
    }

    @Mixins( FooBarInstance.class )
    @Activators( { GammaFooActivator.class, DeltaFooActivator.class } )
    public static interface FooBarServiceWithActivators
            extends FooBar, ServiceComposite
    {
    }

    public static interface FooBar
    {

        String foo();

    }

    public static abstract class FooBarInstance
            implements FooBar
    {

        public String foo()
        {
            return "bar";
        }

    }

    //
    // Activators in order: Alpha, Beta, Gamma, Delta, Epsilon, Zeta ------
    //
    public static class AlphaFooActivator
            extends OrderTestActivator<ServiceReference<FooBarService>>
    {

        public AlphaFooActivator()
        {
            super( "Alpha", RECORDER );
        }

    }

    public static class BetaFooActivator
            extends OrderTestActivator<ServiceReference<FooBarService>>
    {

        public BetaFooActivator()
        {
            super( "Beta", RECORDER );
        }

    }

    public static class GammaFooActivator
            extends OrderTestActivator<ServiceReference<FooBarService>>
    {

        public GammaFooActivator()
        {
            super( "Gamma", RECORDER );
        }

    }

    public static class DeltaFooActivator
            extends OrderTestActivator<ServiceReference<FooBarService>>
    {

        public DeltaFooActivator()
        {
            super( "Delta", RECORDER );
        }

    }

    public static class EpsilonFooActivator
            extends OrderTestActivator<ServiceReference<FooBarService>>
    {

        public EpsilonFooActivator()
        {
            super( "Epsilon", RECORDER );
        }

    }

    public static class ZetaFooActivator
            extends OrderTestActivator<ServiceReference<FooBarService>>
    {

        public ZetaFooActivator()
        {
            super( "Zeta", RECORDER );
        }

    }

    //
    // Tests --------------------------------------------------------------
    //
    @Test
    public void testTwoActivatorsOrderOnSimpleService()
            throws Exception
    {
        new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.services( FooBarService.class ).
                        withActivators( AlphaFooActivator.class, BetaFooActivator.class ).
                        instantiateOnStartup();
            }

        }.application().passivate();

        String actual = Arrays.toString( RECORDER.steps().toArray() );
        System.out.println( "\n" + Expected.ALPHA_BETA_SINGLE + "\n" + actual + "\n" );
        assertEquals( Expected.ALPHA_BETA_SINGLE, actual );
    }

    @Test
    public void testAnnotationActivatorsOrderOnSimpleService()
            throws Exception
    {
        new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.services( FooBarServiceWithActivators.class ).
                        instantiateOnStartup();
            }

        }.application().passivate();

        String expected = Arrays.toString( new String[]{
                    "Gamma.beforeActivation",
                    "Delta.beforeActivation",
                    // -> Activation
                    "Gamma.afterActivation",
                    "Delta.afterActivation",
                    // -> Active
                    "Delta.beforePassivation",
                    "Gamma.beforePassivation",
                    // -> Passivation
                    "Delta.afterPassivation",
                    "Gamma.afterPassivation"
                } );

        String actual = Arrays.toString( RECORDER.steps().toArray() );
        System.out.println( "\n" + expected + "\n" + actual + "\n" );
        assertEquals( expected, actual );
    }

    @Test
    public void testMixedAnnotationAndAssemblyActivatorsOrderOnSimpleService()
            throws Exception
    {
        new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.services( FooBarServiceWithActivators.class ).
                        withActivators( AlphaFooActivator.class, BetaFooActivator.class ).
                        instantiateOnStartup();
            }

        }.application().passivate();

        String expected = Arrays.toString( new String[]{
                    "Alpha.beforeActivation",
                    "Beta.beforeActivation",
                    "Gamma.beforeActivation",
                    "Delta.beforeActivation",
                    // -> Activation
                    "Alpha.afterActivation",
                    "Beta.afterActivation",
                    "Gamma.afterActivation",
                    "Delta.afterActivation",
                    // -> Active
                    "Delta.beforePassivation",
                    "Gamma.beforePassivation",
                    "Beta.beforePassivation",
                    "Alpha.beforePassivation",
                    // -> Passivation
                    "Delta.afterPassivation",
                    "Gamma.afterPassivation",
                    "Beta.afterPassivation",
                    "Alpha.afterPassivation"
                } );

        String actual = Arrays.toString( RECORDER.steps().toArray() );
        System.out.println( "\n" + expected + "\n" + actual + "\n" );
        assertEquals( expected, actual );
    }

}
