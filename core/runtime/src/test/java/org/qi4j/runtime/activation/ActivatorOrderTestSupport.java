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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.qi4j.api.activation.Activator;

public class ActivatorOrderTestSupport
{

    //
    // Common expected results --------------------------------------------
    //
    public static interface Expected
    {

        String ALPHA_BETA_SINGLE = Arrays.toString( new String[]{
                    "Alpha.beforeActivation",
                    "Beta.beforeActivation",
                    // -> Activation
                    "Alpha.afterActivation",
                    "Beta.afterActivation",
                    // -> Active
                    "Beta.beforePassivation",
                    "Alpha.beforePassivation",
                    // -> Passivation
                    "Beta.afterPassivation",
                    "Alpha.afterPassivation"
                } );

    }

    //
    // ActivationStep recorder --------------------------------------------
    //
    public static final class ActivationStep
    {

        public final String activator;

        public final String step;

        public ActivationStep( String activator, String step )
        {
            this.activator = activator;
            this.step = step;
        }

        @Override
        public String toString()
        {
            return activator + "." + step;
        }

    }

    public static interface ActivationStepsRecorder
    {

        void reset();

        void record( ActivationStep step );

        List<ActivationStep> steps();

    }

    public static class ActivationStepsRecorderInstance
            implements ActivationStepsRecorder
    {

        private List<ActivationStep> steps = new ArrayList<ActivationStep>();

        @Override
        public void reset()
        {
            steps = new ArrayList<ActivationStep>();
        }

        @Override
        public void record( ActivationStep step )
        {
            steps.add( step );
        }

        @Override
        public List<ActivationStep> steps()
        {
            return Collections.unmodifiableList( steps );
        }

    }

    //
    // Activator that call the ActivationStepsRecorder --------------------
    //
    public static abstract class OrderTestActivator<T>
            implements Activator<T>
    {

        private final String activator;

        private final ActivationStepsRecorder recorder;

        public OrderTestActivator( String activator, ActivationStepsRecorder recorder )
        {
            this.activator = activator;
            this.recorder = recorder;
        }

        @Override
        public final void beforeActivation( T activating )
                throws Exception
        {
            recorder.record( new ActivationStep( activator, "beforeActivation" ) );
        }

        @Override
        public final void afterActivation( T activated )
                throws Exception
        {
            recorder.record( new ActivationStep( activator, "afterActivation" ) );
        }

        @Override
        public final void beforePassivation( T passivating )
                throws Exception
        {
            recorder.record( new ActivationStep( activator, "beforePassivation" ) );
        }

        @Override
        public final void afterPassivation( T passivated )
                throws Exception
        {
            recorder.record( new ActivationStep( activator, "afterPassivation" ) );
        }

    }

}
