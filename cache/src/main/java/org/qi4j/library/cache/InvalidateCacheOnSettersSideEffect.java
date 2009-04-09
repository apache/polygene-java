package org.qi4j.library.cache;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.sideeffect.GenericSideEffect;
import org.qi4j.library.beans.properties.Setters;

import java.lang.reflect.Method;

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
