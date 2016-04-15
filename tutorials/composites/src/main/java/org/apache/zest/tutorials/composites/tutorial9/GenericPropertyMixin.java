/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.zest.tutorials.composites.tutorial9;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.apache.zest.api.common.AppliesTo;
import org.apache.zest.api.common.AppliesToFilter;
import org.apache.zest.api.injection.scope.State;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.property.StateHolder;

/**
 * JAVADOC
 */
// START SNIPPET: solution
@AppliesTo( { GenericPropertyMixin.PropertyFilter.class } )
public class GenericPropertyMixin
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
// END SNIPPET: solution
