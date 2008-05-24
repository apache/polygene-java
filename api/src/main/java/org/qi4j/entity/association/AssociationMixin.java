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

package org.qi4j.entity.association;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.AppliesToFilter;
import org.qi4j.composite.State;
import org.qi4j.composite.scope.AssociationField;

/**
 * Generic mixin for associations.
 */
@AppliesTo( { AssociationMixin.AssocationFilter.class } )
public class AssociationMixin
    implements InvocationHandler
{
    @AssociationField State associations;

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        return associations.getAssociation( method );
    }

    public static class AssocationFilter
        implements AppliesToFilter
    {
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> modifierClass )
        {
            return AbstractAssociation.class.isAssignableFrom( method.getReturnType() );
        }
    }
}
