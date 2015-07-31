/*
 * Copyright (c) 2011-2012, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.api.association;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.injection.scope.State;

/**
 * Generic mixin for NamedAssociations.
 */
@AppliesTo( NamedAssociationMixin.AssociationFilter.class )
public final class NamedAssociationMixin
    implements InvocationHandler
{
    @State
    private AssociationStateHolder associations;

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        return associations.namedAssociationFor( method );
    }

    /**
     * NamedAssociations generic mixin AppliesToFilter.
     */
    public static class AssociationFilter
        implements AppliesToFilter
    {
        @Override
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> modifierClass )
        {
            return NamedAssociation.class.isAssignableFrom( method.getReturnType() );
        }
    }

}
