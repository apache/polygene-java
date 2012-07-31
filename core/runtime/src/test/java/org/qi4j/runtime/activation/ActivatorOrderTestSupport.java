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

        public void reset()
        {
            steps = new ArrayList<ActivationStep>();
        }

        public void record( ActivationStep step )
        {
            steps.add( step );
        }

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

        public final void beforeActivation( T activating )
                throws Exception
        {
            recorder.record( new ActivationStep( activator, "beforeActivation" ) );
        }

        public final void afterActivation( T activated )
                throws Exception
        {
            recorder.record( new ActivationStep( activator, "afterActivation" ) );
        }

        public final void beforePassivation( T passivating )
                throws Exception
        {
            recorder.record( new ActivationStep( activator, "beforePassivation" ) );
        }

        public final void afterPassivation( T passivated )
                throws Exception
        {
            recorder.record( new ActivationStep( activator, "afterPassivation" ) );
        }

    }

}
