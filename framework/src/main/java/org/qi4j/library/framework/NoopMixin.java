package org.qi4j.library.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Generic mixin that is a no-op. Can be useful if the functionality
 * of a method is mainly provided by assertions and side-effects.
 */
public class NoopMixin
    implements InvocationHandler
{
    public Object invoke( Object object, Method method, Object[] args ) throws Throwable
    {
        return method.getDefaultValue();
    }
}
