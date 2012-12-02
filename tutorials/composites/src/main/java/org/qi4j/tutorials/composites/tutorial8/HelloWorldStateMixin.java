package org.qi4j.tutorials.composites.tutorial8;

import org.qi4j.api.injection.scope.State;
import org.qi4j.api.property.Property;

// START SNIPPET: solution

/**
 * This is the implementation of the HelloWorld
 * state interface.
 */
public class HelloWorldStateMixin
    implements HelloWorldState
{
    @State
    private Property<String> phrase;
    @State
    private Property<String> name;

    @Override
    public Property<String> phrase()
    {
        return phrase;
    }

    @Override
    public Property<String> name()
    {
        return name;
    }
}
// END SNIPPET: solution
