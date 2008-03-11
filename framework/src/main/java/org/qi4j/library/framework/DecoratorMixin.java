package org.qi4j.library.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.qi4j.composite.scope.Uses;

/**
 * Generic decorator mixin that allows a Composite to wrap
 * any other Composite as long as they share an interface.
 * <p/>
 * Can be used to effectively implement
 * singleton mixins, since the decorated object can be shared between
 * many instances.
 */
public class DecoratorMixin
    implements InvocationHandler
{
    @Uses Object delegate;

    public Object invoke( Object object, Method method, Object[] args ) throws Throwable
    {
        if( delegate instanceof InvocationHandler )
        {
            InvocationHandler handler = (InvocationHandler) delegate;
            return handler.invoke( object, method, args );
        }
        else
        {
            try
            {
                return method.invoke( delegate, args );
            }
            catch( InvocationTargetException e )
            {
                throw e.getCause();
            }
        }
    }
}
