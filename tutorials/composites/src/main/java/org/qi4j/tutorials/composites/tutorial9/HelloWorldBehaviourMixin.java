package org.qi4j.tutorials.composites.tutorial9;

import org.qi4j.api.injection.scope.This;

// START SNIPPET: solution

/**
 * This is the implementation of the HelloWorld
 * behaviour interface.
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
