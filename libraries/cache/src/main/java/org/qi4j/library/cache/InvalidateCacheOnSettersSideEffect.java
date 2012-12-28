package org.qi4j.library.cache;

import java.lang.reflect.Method;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.sideeffect.GenericSideEffect;

/**
 * Invalidate cache on setters.
 */
@AppliesTo( InvalidateCacheOnSettersSideEffect.AppliesTo.class )
public class InvalidateCacheOnSettersSideEffect extends GenericSideEffect
{
    public static class AppliesTo
        implements AppliesToFilter
    {
        @Override
        public boolean appliesTo( Method method, Class mixin, Class compositeType, Class modifierClass )
        {
            return !( method.getDeclaringClass().equals( InvocationCache.class ) ||
                      method.getDeclaringClass().equals( InvocationCacheMixin.class ) );

        }
    }

    @This private InvocationCache cache;

    @Override
    protected void invoke( Method method, Object[] args )
    {
        cache.clearCachedValues();
    }
}
