package org.qi4j.runtime;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.This;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Unit tests related to private mixins.
 */
public class PrivateMixinTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( SpeakComposite.class );
    }

    /**
     * Tests that private mixins are injected correctly.
     */
    @Test
    public void privateMixinInjection()
    {
        SpeakComposite test = compositeBuilderFactory.newCompositeBuilder( SpeakComposite.class ).newInstance();
        assertThat( "Speak", test.speak(), is( equalTo( "I say it works" ) ) );
    }

    @Mixins( SpeakMixin.class )
    public interface Speak
    {
        String speak();
    }

    public static class SpeakMixin
        implements Speak
    {
        @This Word word;

        public String speak()
        {
            return "I say " + word.get();
        }
    }

    @Mixins( { WordMixin.class } )
    public interface SpeakComposite
        extends Speak, Composite
    {
    }

    public interface Word
    {
        String get();
    }

    public static class WordMixin
        implements Word
    {
        public String get()
        {
            return "it works";
        }
    }

}
