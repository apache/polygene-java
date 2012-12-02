package org.qi4j.tutorials.composites.tutorial2;

// START SNIPPET: solution

/**
 * This is the implementation of the HelloWorld
 * interface. The behaviour and state is mixed.
 */
public class HelloWorldMixin
    implements HelloWorld
{
    String phrase;
    String name;

    @Override
    public String say()
    {
        return getPhrase() + " " + getName();
    }

    @Override
    public void setPhrase( String phrase )
        throws IllegalArgumentException
    {
        if( phrase == null )
        {
            throw new IllegalArgumentException( "Phrase may not be null" );
        }

        this.phrase = phrase;
    }

    @Override
    public String getPhrase()
    {
        return phrase;
    }

    @Override
    public void setName( String name )
        throws IllegalArgumentException
    {
        if( name == null )
        {
            throw new IllegalArgumentException( "Name may not be null" );
        }

        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }
}
// END SNIPPET: solution
