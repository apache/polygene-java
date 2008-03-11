package org.qi4j.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.composite.Composite;
import org.qi4j.composite.scope.ThisCompositeAs;

public class Qi4jTestMixin
    implements InvocationHandler
{

    @ThisCompositeAs Composite composite;

    public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
    {
        Object mock = Qi4jTestSetup.getMock( o );
        if( mock == null )
        {
            throw new IllegalStateException( "Mock not registered for " + composite.getCompositeType() );
        }
        Method mockMethod = mock.getClass().getMethod( method.getName(), method.getParameterTypes() );
        if( mockMethod == null )
        {
            throw new IllegalStateException( "Expected method not found in mock: " + method );
        }
        return mockMethod.invoke( mock, objects );
    }

}