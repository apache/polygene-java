package org.qi4j.test.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public interface Mock
{
    InvocationHandler getInvocationHandler( Object proxy, Method method, Object[] args );
}