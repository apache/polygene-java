package org.qi4j.library.cache;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.sideeffect.GenericSideEffect;

/**
 * Cache result of @Cached method calls.
 */
@AppliesTo( Cached.class )
public class CacheInvocationResultSideEffect extends GenericSideEffect
{
    @This private InvocationCache cache;
    @Invocation private Method method;

    @Override
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        // Get value
        // if an exception is thrown, don't do anything
        Object res = result.invoke( proxy, method, args );
        if( res == null )
        {
            res = Void.TYPE;
        }

        String cacheName = method.getName();
        if( args != null )
        {
            cacheName += Arrays.asList( args );
        }
        Object oldResult = cache.cachedValue( cacheName );
        if( oldResult == null || !oldResult.equals( result ) )
        {
            cache.setCachedValue( cacheName, result );
        }
        return result;
    }
}
