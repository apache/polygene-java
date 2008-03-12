package org.qi4j.test.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class UnplayableMock
    implements Mock
{
    public InvocationHandler getInvocationHandler( Object proxy, Method method, Object[] args )
    {
        throw new IllegalStateException( "Undefined" );
    }
}