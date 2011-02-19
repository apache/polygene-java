package org.qi4j.tutorials.composites.tutorial2;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class HelloWorldTest
{
    HelloWorld helloWorld;

    @Before
    public void setUp()
        throws Exception
    {
        helloWorld = new HelloWorldMixin();
    }

    @Test
    public void givenHelloWorldWhenSetPropertiesAndSayThenReturnCorrectResult()
    {
        {
            helloWorld.setPhrase( "Hello" );
            helloWorld.setName( "World" );
            String result = helloWorld.say();
            assertThat( result, equalTo( "Hello World" ) );
        }

        {
            helloWorld.setPhrase( "Hey" );
            helloWorld.setName( "Universe" );
            String result = helloWorld.say();
            assertThat( result, equalTo( "Hey Universe" ) );
        }
    }

    @Test
    public void givenHelloWorldWhenSetInvalidPhraseThenThrowException()
    {
        try
        {
            helloWorld.setPhrase( null );
            fail( "Should not be allowed to set phrase to null" );
        }
        catch( IllegalArgumentException e )
        {
            // Ok
        }
    }

    @Test
    public void givenHelloWorldWhenSetInvalidNameThenThrowException()
    {
        try
        {
            helloWorld.setName( null );
            fail( "Should not be allowed to set name to null" );
        }
        catch( IllegalArgumentException e )
        {
            // Ok
        }
    }
}
