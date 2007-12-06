package org.qi4j.library.framework.caching;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.ConcernFor;
import org.qi4j.composite.Invocation;
import org.qi4j.composite.ThisCompositeAs;

/**
 * Return value of @Cached calls on exceptions.
 * <p/>
 * If an Exception occurs, try to reuse a previous result. Don't do anything on Throwables.
 */
@AppliesTo( Cached.class )
public class ReturnCachedValueOnExceptionConcern
    implements InvocationHandler
{
    @ThisCompositeAs private InvocationCache cache;
    @Invocation private Method method;
    @ConcernFor private InvocationHandler next;

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        try
        {
            // Invoke method
            return next.invoke( proxy, method, args );
        }
        catch( Exception e )
        {
            // Try cache
            String cacheName = method.getName();
            if( args != null )
            {
                cacheName += Arrays.asList( args );
            }
            Object result = cache.getCachedValue( cacheName );
            if( result != null )
            {
                if( result == Void.TYPE )
                {
                    return null;
                }
                else
                {
                    return result;
                }
            }

            throw e;
        }
    }
}
