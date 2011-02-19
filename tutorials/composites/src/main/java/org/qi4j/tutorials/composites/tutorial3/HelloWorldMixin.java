package org.qi4j.tutorials.composites.tutorial3;

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

    public String say()
    {
        return phrase + " " + name;
    }

    public String getPhrase()
    {
        return phrase;
    }

    public void setPhrase( String phrase )
    {
        this.phrase = phrase;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }
}
