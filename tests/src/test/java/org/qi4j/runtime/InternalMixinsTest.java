package org.qi4j.runtime;

import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.test.Qi4jTestSetup;

/**
 * TODO
 */
public class InternalMixinsTest
    extends Qi4jTestSetup
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( TestComposite.class );
    }

    @Test
    public void testInternalMixins()
    {
        TestComposite test = compositeBuilderFactory.newCompositeBuilder( TestComposite.class ).newInstance();
        assertEquals( "XYZ123", test.doStuff() );
    }

    @Mixins( AMixin.class )
    public interface A
    {
        String doStuff();
    }

    public static class AMixin
        implements A
    {
        @ThisCompositeAs B bRef;

        public String doStuff()
        {
            return bRef.otherStuff() + "123";
        }
    }

    public interface B
    {
        String otherStuff();
    }

    public static class BMixin
        implements B
    {
        public String otherStuff()
        {
            return "XYZ";
        }
    }

    @Mixins( { InternalMixinsTest.BMixin.class } )
    public interface TestComposite
        extends A, Composite
    {
    }
}
