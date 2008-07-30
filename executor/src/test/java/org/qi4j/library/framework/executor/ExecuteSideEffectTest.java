package org.qi4j.library.framework.executor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import static junit.framework.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.SideEffects;
import org.qi4j.library.executor.ExecuteService;
import org.qi4j.library.executor.ExecuteSideEffect;
import org.qi4j.library.executor.ExecutorSideEffect;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class ExecuteSideEffectTest
    extends AbstractQi4jTest
{
    private static final CountDownLatch latch = new CountDownLatch( 1 );

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( TestComposite.class );
        module.addObjects( LogCall.class );
        module.addServices( ExecuteService.class ).instantiateOnStartup();
    }

    @Test
    public void givenMethodWithAnnotationWhenCallThenExecuteSideEffect()
        throws InterruptedException
    {
        TestComposite instance = compositeBuilderFactory.newComposite( TestComposite.class );
        System.out.println( instance.doStuff() );
        latch.await();
        assertTrue( "doStuff sideeffect called", LogCall.methodsCalled.contains( "doStuff" ) );
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
        static Collection<String> methodsCalled = new ArrayList<String>();

        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            methodsCalled.add( method.getName() );
            System.out.println( "method = " + method );
            latch.countDown();
            return null;
        }
    }
}
