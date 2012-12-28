package org.qi4j.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * JAVADOC
 */
public final class SideEffectInvocationHandlerResult
    implements InvocationHandler
{
    private Object result;
    private Throwable throwable;

    public SideEffectInvocationHandlerResult()
    {
    }

    public void setResult( Object result, Throwable throwable )
    {
        this.result = result;
        this.throwable = throwable;
    }

    // InvocationHandler implementation ------------------------------

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        if( throwable != null )
        {
            throw throwable;
        }
        else
        {
            return result;
        }
    }
}
