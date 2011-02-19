package org.qi4j.tutorials.composites.tutorial8;

import org.qi4j.api.injection.scope.State;
import org.qi4j.api.property.Property;

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

    public Property<String> phrase()
    {
        return phrase;
    }

    public Property<String> name()
    {
        return name;
    }
}