package org.qi4j.tutorials.composites.tutorial10;

import org.qi4j.api.injection.scope.This;

// START SNIPPET: solution

/**
 * This is the implementation of the say() method. The mixin
 * is abstract so it doesn't have to implement all methods
 * from the Composite interface.
 */
public abstract class HelloWorldMixin
    implements HelloWorldComposite
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
