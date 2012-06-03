package org.qi4j.tutorials.composites.tutorial1;

// START SNIPPET: initial

/**
 * Initial HelloWorld implementation. Everything is mixed up
 * into one class, and no interface is used.
 */
public class HelloWorld
{
    String phrase;
    String name;

    public String getPhrase()
    {
        return phrase;
    }

    public void setPhrase( String phrase )
        throws IllegalArgumentException
    {
        if( phrase == null )
        {
            throw new IllegalArgumentException( "Phrase may not be null " );
        }

        this.phrase = phrase;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
        throws IllegalArgumentException
    {
        if( name == null )
        {
            throw new IllegalArgumentException( "Name may not be null " );
        }

        this.name = name;
    }

    public String say()
    {
        return phrase + " " + name;
    }
}
// END SNIPPET: initial
