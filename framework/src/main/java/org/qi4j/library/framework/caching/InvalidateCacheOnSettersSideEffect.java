package org.qi4j.library.framework.caching;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.AppliesToFilter;
import org.qi4j.composite.SideEffectFor;
import org.qi4j.composite.ThisCompositeAs;
import org.qi4j.library.framework.properties.Setters;

/**
 * Invalidate cache on setters.
 */
@AppliesTo( InvalidateCacheOnSettersSideEffect.AppliesTo.class )
public class InvalidateCacheOnSettersSideEffect
    implements InvocationHandler
{
    public static class AppliesTo
        implements AppliesToFilter
    {
        public boolean appliesTo( Method method, Class mixin, Class compositeType, Class modifierClass )
        {
            if( method.getDeclaringClass().equals( InvocationCache.class ) )
            {
                return false;
            }

            return new Setters().appliesTo( method, mixin, compositeType, modifierClass );
        }
    }

    @SideEffectFor InvocationHandler next;
    @ThisCompositeAs private InvocationCache cache;

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        cache.clearCachedValues();
        return null;
    }
}
