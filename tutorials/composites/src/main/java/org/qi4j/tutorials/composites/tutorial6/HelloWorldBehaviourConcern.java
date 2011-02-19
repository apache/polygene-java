package org.qi4j.tutorials.composites.tutorial6;

import org.qi4j.api.concern.ConcernOf;

/**
 * This Concern validates the parameters
 * to the HelloWorldState interface.
 */
public class HelloWorldBehaviourConcern
    extends ConcernOf<HelloWorldBehaviour>
    implements HelloWorldBehaviour
{
    public String say()
    {
        return "Simon says:" + next.say();
    }
}
