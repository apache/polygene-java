package org.qi4j.tutorials.composites.tutorial6;

import org.qi4j.api.concern.ConcernOf;

// START SNIPPET: solution

/**
 * This Concern validates the parameters
 * to the HelloWorldState interface.
 */
public class HelloWorldBehaviourConcern
    extends ConcernOf<HelloWorldBehaviour>
    implements HelloWorldBehaviour
{
    @Override
    public String say()
    {
        return "Simon says:" + next.say();
    }
}
// END SNIPPET: solution
