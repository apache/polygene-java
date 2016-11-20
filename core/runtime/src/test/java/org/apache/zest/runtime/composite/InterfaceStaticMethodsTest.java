package org.apache.zest.runtime.composite;

import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.property.Property;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Assert that interface static methods are ignored when assembling composites.
 */
public class InterfaceStaticMethodsTest extends AbstractZestTest
{
    public interface StaticMethods
    {
        @UseDefaults( "foo" )
        Property<String> foo();

        static String bar()
        {
            return "bar";
        }
    }

    public interface OverrideStaticMethods extends StaticMethods
    {
        static String bar()
        {
            return "bar overridden";
        }
    }

    @Override
    public void assemble( final ModuleAssembly module ) throws AssemblyException
    {
        module.transients( StaticMethods.class, OverrideStaticMethods.class );
    }

    @Test
    public void staticMethods() throws NoSuchMethodException
    {
        StaticMethods staticMethods = transientBuilderFactory.newTransient( StaticMethods.class );

        assertThat( staticMethods.foo().get(), equalTo( "foo" ) );
        assertThat( StaticMethods.bar(), equalTo( "bar" ) );
    }

    @Test
    public void overrideStaticMethods()
    {
        OverrideStaticMethods staticMethods = transientBuilderFactory.newTransient( OverrideStaticMethods.class );

        assertThat( staticMethods.foo().get(), equalTo( "foo" ) );
        assertThat( OverrideStaticMethods.bar(), equalTo( "bar overridden" ) );
    }
}
