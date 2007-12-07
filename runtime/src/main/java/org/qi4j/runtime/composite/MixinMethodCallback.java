package org.qi4j.runtime.composite;

import java.lang.reflect.Method;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * TODO
 */
public class MixinMethodCallback
    implements MethodInterceptor
{
    public Object intercept( Object object, Method method, Object[] objects, MethodProxy methodProxy ) throws Throwable
    {
        return null;
    }
}
