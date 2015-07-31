/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.api.property;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.injection.scope.State;

/**
 * Generic mixin for properties.
 */
// START SNIPPET: actual
@AppliesTo( { PropertyMixin.PropertyFilter.class } )
public final class PropertyMixin
    implements InvocationHandler
{
    @State
    private StateHolder state;

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        return state.propertyFor( method );
    }

    /**
     * Filter Property methods to apply generic Property Mixin.
     */
    public static class PropertyFilter
        implements AppliesToFilter
    {
        @Override
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> modifierClass )
        {
            return Property.class.isAssignableFrom( method.getReturnType() );
        }
    }
}
// END SNIPPET: actual
