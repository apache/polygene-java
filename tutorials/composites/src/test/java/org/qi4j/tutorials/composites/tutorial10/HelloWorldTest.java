package org.qi4j.tutorials.composites.tutorial10;

import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class HelloWorldTest
{
    private SingletonAssembler assembly;

    @Before
    public void setUp()
        throws Exception
    {
        assembly = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( HelloWorldComposite.class );
            }
        };
    }

    @Test
    public void givenAssemblyWhenBuildInstanceAndSayThenReturnCorrectResult()
    {
        {
            TransientBuilder<HelloWorldComposite> builder =
                assembly.module().newTransientBuilder( HelloWorldComposite.class );
            builder.prototypeFor( HelloWorldState.class ).phrase().set( "Hello" );
            builder.prototypeFor( HelloWorldState.class ).name().set( "World" );
            HelloWorldComposite helloWorld = builder.newInstance();
            String result = helloWorld.say();
            assertThat( result, equalTo( "Hello World" ) );
        }

        {
            TransientBuilder<HelloWorldComposite> builder =
                assembly.module().newTransientBuilder( HelloWorldComposite.class );
            builder.prototypeFor( HelloWorldState.class ).phrase().set( "Hey" );
            builder.prototypeFor( HelloWorldState.class ).name().set( "Universe" );
            HelloWorldComposite helloWorld = builder.newInstance();
            String result = helloWorld.say();
            assertThat( result, equalTo( "Hey Universe" ) );
        }
    }

    @Test
    public void givenAssemblyWhenSetInvalidPhraseThenThrowException()
    {
        try
        {
            TransientBuilder<HelloWorldComposite> builder =
                assembly.module().newTransientBuilder( HelloWorldComposite.class );
            builder.prototypeFor( HelloWorldState.class ).phrase().set( null );
            HelloWorldComposite helloWorld = builder.newInstance();

            fail( "Should not be allowed to set phrase to null" );
        }
        catch( IllegalArgumentException e )
        {
            // Ok
        }

        try
        {
            TransientBuilder<HelloWorldComposite> builder =
                assembly.module().newTransientBuilder( HelloWorldComposite.class );
            builder.prototypeFor( HelloWorldState.class ).phrase().set( "" );
            HelloWorldComposite helloWorld = builder.newInstance();

            fail( "Should not be allowed to set phrase to empty string" );
        }
        catch( IllegalArgumentException e )
        {
            // Ok
        }
    }

    @Test
    public void givenAssemblyWhenSetInvalidNameThenThrowException()
    {
        try
        {
            TransientBuilder<HelloWorldComposite> builder =
                assembly.module().newTransientBuilder( HelloWorldComposite.class );
            builder.prototypeFor( HelloWorldState.class ).name().set( null );
            HelloWorldComposite helloWorld = builder.newInstance();

            fail( "Should not be allowed to set phrase to null" );
        }
        catch( IllegalArgumentException e )
        {
            // Ok
        }

        try
        {
            TransientBuilder<HelloWorldComposite> builder =
                assembly.module().newTransientBuilder( HelloWorldComposite.class );
            builder.prototypeFor( HelloWorldState.class ).name().set( "" );
            HelloWorldComposite helloWorld = builder.newInstance();

            fail( "Should not be allowed to set phrase to empty string" );
        }
        catch( IllegalArgumentException e )
        {
            // Ok
        }
    }
}