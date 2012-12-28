package org.qi4j.runtime.composite;

import org.junit.Test;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class QI247Test1
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TransientWithMixin.class );
    }

    private void checkToString( ObjectMethods withMixin )
    {
        assertEquals( ObjectMethods.MESSAGE, withMixin.toString() );
    }

    private void checkHashCode( ObjectMethods withMixin )
    {
        assertEquals( ObjectMethods.CODE, withMixin.hashCode() );
    }

    private void checkSelfEquals( ObjectMethods withMixin )
    {
        assertEquals( withMixin, withMixin );
    }

    private void checkTwoNotEqual( ObjectMethods first, ObjectMethods second )
    {
        assertFalse( first.equals( second ) );
    }

    // MIXIN

    @Test
    public void testWithMixinToString()
    {
        ObjectMethods withMixin = module.newTransient( ObjectMethods.class );
        checkToString( withMixin );
    }

    @Test
    public void testWithMixinHashCode()
    {
        ObjectMethods withMixin = module.newTransient( ObjectMethods.class );
        checkHashCode( withMixin );
    }

    @Test
    public void testWithMixinSelfEquals()
    {
        ObjectMethods withMixin = module.newTransient( ObjectMethods.class );
        checkSelfEquals( withMixin );
    }

    @Test
    public void testWithMixinSelfEquals2()
    {
        ObjectMethods withMixin = module.newTransient( ObjectMethods.class );
        assertTrue( withMixin.equals( withMixin ) );
    }

    @Test
    public void testWithMixinSelfSame()
    {
        ObjectMethods withMixin = module.newTransient( ObjectMethods.class );
        assertSame( withMixin, withMixin );
    }

    @Test
    public void testWithMixinTwoNotEqual()
    {
        ObjectMethods first = module.newTransient( ObjectMethods.class );
        ObjectMethods second = module.newTransient( ObjectMethods.class );
        checkTwoNotEqual( first, second );
    }

    public interface ObjectMethods
    {
        String MESSAGE = "Does not work :(";
        int CODE = 123;

        void someMethod();
    }

    public static class ObjectMethodsMixin
        implements ObjectMethods
    {

        @Override
        public int hashCode()
        {
            return CODE;
        }

        @Override
        public String toString()
        {
            return MESSAGE;
        }

        public void someMethod()
        {
        }
    }

    @Mixins( ObjectMethodsMixin.class )
    public interface TransientWithMixin
        extends TransientComposite, ObjectMethods
    {
    }
}
