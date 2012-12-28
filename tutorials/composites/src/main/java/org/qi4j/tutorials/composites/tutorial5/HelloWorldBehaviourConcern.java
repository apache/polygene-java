package org.qi4j.tutorials.composites.tutorial5;

import org.qi4j.api.concern.ConcernOf;

// START SNIPPET: solution

/**
 * This is a concern that modifies the mixin behaviour.
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
