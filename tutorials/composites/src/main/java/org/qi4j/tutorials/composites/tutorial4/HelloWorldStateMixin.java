package org.qi4j.tutorials.composites.tutorial4;

// START SNIPPET: solution

/**
 * This is the implementation of the HelloWorld
 * state interface.
 */
public class HelloWorldStateMixin
    implements HelloWorldState
{
    String phrase;
    String name;

    @Override
    public String getPhrase()
    {
        return phrase;
    }

    @Override
    public void setPhrase( String phrase )
    {
        this.phrase = phrase;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName( String name )
    {
        this.name = name;
    }
}
// END SNIPPET: solution
