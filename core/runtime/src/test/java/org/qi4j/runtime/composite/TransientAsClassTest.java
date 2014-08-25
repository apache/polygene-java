package org.qi4j.runtime.composite;

import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test for QI-298.
 */
@Ignore( "Awaiting QI-298" )
public class TransientAsClassTest
    extends AbstractQi4jTest
{
    public static class UnderTest
    {
        public String foo()
        {
            return "bar";
        }
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( UnderTest.class );
    }

    @Test
    public void test()
    {
        UnderTest underTest = module.newTransient( UnderTest.class );
        assertThat( underTest.foo(), equalTo( "bar" ) );
    }
}
