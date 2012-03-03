package org.qi4j.regression.qi65;

import org.junit.Test;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

public class IssueTest
    extends AbstractQi4jTest
{
    private final static Class<?> CLAZZ = Object.class;
    private final static String METHOD_NAME = "toString";
    private final static Class<?> PARAM_TYPES[] = { };

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TestComposite.class );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constraintOnMethodParameter()
        throws SecurityException, NoSuchMethodException
    {
        TestComposite test = module.newTransient( TestComposite.class );

        test.someMethod( null );
    }

    @Mixins( TestMixin.class )
    public interface TestComposite
        extends TransientComposite
    {
        String someMethod( String value );
    }

    public static abstract class TestMixin
        implements TestComposite
    {
        public String someMethod( String value )
        {
            return value + " " + value;
        }
    }
}