package org.qi4j.tutorials.composites.tutorial3;

// START SNIPPET: solution

/**
 * This is the implementation of the HelloWorld
 * interface. The behaviour and state is mixed. Since parameters
 * are mandatory as default in Qi4j there's no need to do null checks.
 */
public class HelloWorldMixin
    implements HelloWorld
{
    String phrase;
    String name;

    @Override
    public String say()
    {
        return phrase + " " + name;
    }

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
