package org.qi4j.tutorials.composites.tutorial8;

import org.qi4j.api.injection.scope.This;

// START SNIPPET: solution

/**
 * This is the implementation of the HelloWorld
 * behaviour interface.
 * <p/>
 * This version access the state using Qi4j Properties.
 */
public class HelloWorldBehaviourMixin
    implements HelloWorldBehaviour
{
    @This
    HelloWorldState state;

    @Override
    public String say()
    {
        return state.phrase().get() + " " + state.name().get();
    }
}
// END SNIPPET: solution
