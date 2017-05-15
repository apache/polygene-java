package org.apache.polygene.api.composite;

import java.lang.reflect.Method;
import org.apache.polygene.api.common.AppliesToFilter;

/**
 * Filter Default Interface Methods to apply a generic fragment.
 */
public class DefaultMethodsFilter
    implements AppliesToFilter
{
    @Override
    public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> modifierClass )
    {
        return method.isDefault();
    }
}
