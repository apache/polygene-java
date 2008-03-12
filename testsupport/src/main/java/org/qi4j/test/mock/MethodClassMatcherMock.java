package org.qi4j.test.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MethodClassMatcherMock
    implements Mock, InvocationHandler
{

    private final Object recordedMock;
    private final Class methodClass;

    public MethodClassMatcherMock( Object recordedMock, Class methodClass )
    {
        this.recordedMock = recordedMock;
        this.methodClass = methodClass;
    }

    public InvocationHandler getInvocationHandler( Object proxy, Method method, Object[] args )
    {
        if( method.getDeclaringClass().equals( methodClass ) )
        {
            return this;
        }
        return null;
    }

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        return method.invoke( recordedMock, args );
    }
}