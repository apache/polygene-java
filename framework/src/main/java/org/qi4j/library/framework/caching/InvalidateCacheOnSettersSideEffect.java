package org.qi4j.library.framework.caching;

import java.lang.reflect.Method;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.AppliesToFilter;
import org.qi4j.composite.GenericSideEffect;
import org.qi4j.composite.scope.This;
import org.qi4j.library.framework.properties.Setters;

/**
 * Invalidate cache on setters.
 */
@AppliesTo( InvalidateCacheOnSettersSideEffect.AppliesTo.class )
public class InvalidateCacheOnSettersSideEffect extends GenericSideEffect
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

    @This private InvocationCache cache;

    protected void invoke( Method method, Object[] args )
    {
        cache.clearCachedValues();
    }
}
