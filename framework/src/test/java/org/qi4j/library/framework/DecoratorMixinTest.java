package org.qi4j.library.framework;
/**
 *  TODO
 */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import junit.framework.TestCase;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.runtime.CompositeBuilderFactoryImpl;

public class DecoratorMixinTest extends TestCase
{
    public void testGenericDecoratorOfDomainobject() throws Exception
    {
        CompositeBuilderFactory cbf = new CompositeBuilderFactoryImpl();
        CompositeBuilder<Composite1> cb = cbf.newCompositeBuilder( Composite1.class );
        cb.decorate( new Test1.Test1Mixin() );
        Test1 test = cb.newInstance();

        assertEquals( "ok", test.test() );
    }

    public void testGenericDecoratorOfInvocationHandler() throws Exception
    {
        CompositeBuilderFactory cbf = new CompositeBuilderFactoryImpl();
        CompositeBuilder<Composite1> cb = cbf.newCompositeBuilder( Composite1.class );
        cb.decorate( new InvocationHandler()
        {

            public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
            {
                return "ok";
            }
        } );
        Test1 test = cb.newInstance();

        assertEquals( "ok", test.test() );
    }

    interface Test1
    {
        public String test();

        public class Test1Mixin
            implements Test1
        {

            public String test()
            {
                return "ok";
            }
        }
    }

    @ImplementedBy( DecoratorMixin.class )
    interface Composite1 extends Composite, Test1
    {
    }
}