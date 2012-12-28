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
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

import static org.junit.Assert.*;
import static org.qi4j.runtime.activation.ActivatorOrderTestSupport.*;

public class StructureActivatorOrderTest
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
    // Activators in order: Alpha, Beta -----------------------------------
    //
    public static class AlphaApplicationActivator
            extends OrderTestActivator<Application>
    {

        public AlphaApplicationActivator()
        {
            super( "Alpha", RECORDER );
        }

    }

    public static class BetaApplicationActivator
            extends OrderTestActivator<Application>
    {

        public BetaApplicationActivator()
        {
            super( "Beta", RECORDER );
        }

    }

    public static class AlphaLayerActivator
            extends OrderTestActivator<Layer>
    {

        public AlphaLayerActivator()
        {
            super( "Alpha", RECORDER );
        }

    }

    public static class BetaLayerActivator
            extends OrderTestActivator<Layer>
    {

        public BetaLayerActivator()
        {
            super( "Beta", RECORDER );
        }

    }

    public static class AlphaModuleActivator
            extends OrderTestActivator<Module>
    {

        public AlphaModuleActivator()
        {
            super( "Alpha", RECORDER );
        }

    }

    public static class BetaModuleActivator
            extends OrderTestActivator<Module>
    {

        public BetaModuleActivator()
        {
            super( "Beta", RECORDER );
        }

    }

    //
    // Tests --------------------------------------------------------------
    //
    @Test
    public void testTwoActivatorsOrderOnApplication()
            throws Exception
    {
        new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.layer().application().withActivators( AlphaApplicationActivator.class,
                                                             BetaApplicationActivator.class );
            }

        }.application().passivate();

        String actual = Arrays.toString( RECORDER.steps().toArray() );
        System.out.println( "\n" + Expected.ALPHA_BETA_SINGLE + "\n" + actual + "\n" );
        assertEquals( Expected.ALPHA_BETA_SINGLE, actual );
    }

    @Test
    public void testTwoActivatorsOrderOnLayer()
            throws Exception
    {
        new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.layer().withActivators( AlphaLayerActivator.class,
                                               BetaLayerActivator.class );
            }

        }.application().passivate();

        String actual = Arrays.toString( RECORDER.steps().toArray() );
        System.out.println( "\n" + Expected.ALPHA_BETA_SINGLE + "\n" + actual + "\n" );
        assertEquals( Expected.ALPHA_BETA_SINGLE, actual );
    }

    @Test
    public void testTwoActivatorsOrderOnModule()
            throws Exception
    {
        new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.withActivators( AlphaModuleActivator.class,
                                       BetaModuleActivator.class );
            }

        }.application().passivate();

        String actual = Arrays.toString( RECORDER.steps().toArray() );
        System.out.println( "\n" + Expected.ALPHA_BETA_SINGLE + "\n" + actual + "\n" );
        assertEquals( Expected.ALPHA_BETA_SINGLE, actual );
    }

    @Test
    public void testTwoActivatorsOrderOnApplicationLayerAndModule()
            throws Exception
    {
        new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.layer().application().withActivators( AlphaApplicationActivator.class,
                                                             BetaApplicationActivator.class );
                module.layer().withActivators( AlphaLayerActivator.class,
                                               BetaLayerActivator.class );
                module.withActivators( AlphaModuleActivator.class,
                                       BetaModuleActivator.class );
            }

        }.application().passivate();

        String expected = Arrays.toString( new String[]{
                    // Application.beforeActivation
                    "Alpha.beforeActivation",
                    "Beta.beforeActivation",
                    // Layer.beforeActivation
                    "Alpha.beforeActivation",
                    "Beta.beforeActivation",
                    // Module.beforeActivation
                    "Alpha.beforeActivation",
                    "Beta.beforeActivation",
                    //
                    // -> Activation
                    //
                    // Application.afterActivation
                    "Alpha.afterActivation",
                    "Beta.afterActivation",
                    // Layer.afterActivation
                    "Alpha.afterActivation",
                    "Beta.afterActivation",
                    // Module.afterActivation
                    "Alpha.afterActivation",
                    "Beta.afterActivation",
                    //
                    // -> Active
                    //
                    // Module.beforePassivation
                    "Beta.beforePassivation",
                    "Alpha.beforePassivation",
                    // Layer.beforePassivation
                    "Beta.beforePassivation",
                    "Alpha.beforePassivation",
                    // Application.beforePassivation
                    "Beta.beforePassivation",
                    "Alpha.beforePassivation",
                    //
                    // -> Passivation
                    //
                    // Module.afterPassivation
                    "Beta.afterPassivation",
                    "Alpha.afterPassivation",
                    // Layer.afterPassivation
                    "Beta.afterPassivation",
                    "Alpha.afterPassivation",
                    // Application.afterPassivation
                    "Beta.afterPassivation",
                    "Alpha.afterPassivation"
                } );

        String actual = Arrays.toString( RECORDER.steps().toArray() );
        System.out.println( "\n" + expected + "\n" + actual + "\n" );
        assertEquals( expected, actual );
    }

}
