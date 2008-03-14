package org.qi4j.test.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.composite.scope.ThisCompositeAs;

public class MockPlayerMixin
    implements InvocationHandler
{

    @ThisCompositeAs MockRepository mockRepository;

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        System.out.println( "Play mock for " + method );
        for( Mock mock : mockRepository.getAll() )
        {
            InvocationHandler handler = mock.getInvocationHandler( proxy, method, args );
            if( handler != null )
            {
                return handler.invoke( mock, method, args );
            }
        }
        throw new IllegalStateException( "No behavior definition" );
    }

}