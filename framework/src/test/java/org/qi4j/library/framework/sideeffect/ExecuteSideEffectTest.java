package org.qi4j.library.framework.sideeffect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.SideEffects;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class ExecuteSideEffectTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( TestComposite.class );
        module.addObjects( LogCall.class );
        module.addServices( ExecuteService.class ).instantiateOnStartup();
    }

    @Test
    public void givenMethodWithAnnotationWhenCallThenExecuteSideEffect()
    {
        TestComposite instance = compositeBuilderFactory.newComposite( TestComposite.class );

        System.out.println( instance.doStuff() );
    }

    @SideEffects( ExecutorSideEffect.class )
    @Mixins( TestMixin.class )
    public interface TestComposite
        extends Composite
    {
        @ExecuteSideEffect( LogCall.class ) String doStuff();
    }

    public static abstract class TestMixin
        implements TestComposite
    {
        public String doStuff()
        {
            return "Foo";
        }
    }

    public static class LogCall
        implements InvocationHandler
    {
        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            System.out.println( "Called " + method.getName() );
            return null;
        }
    }
}
