package org.qi4j.library.framework.caching;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.scope.ThisAs;
import org.qi4j.library.framework.properties.Setters;

/**
 * Invalidate cache on setters.
 */
@AppliesTo( Setters.class )
public class InvalidateCacheOnSettersSideEffect
    implements InvocationHandler
{
    @ThisAs private InvocationCache cache;

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        cache.clear();
        return null;
    }
}
