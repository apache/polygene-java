/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.bootstrap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.entity.association.AssociationChangeObserver;

/**
 * TODO
 */
public class AssociationBuilder
{
    private boolean aggregate = false;
    private Method accessor;
    private List<AssociationChangeObserver<?, ?>> changeObservers = new ArrayList<AssociationChangeObserver<?, ?>>();
    private Set<Object> associationInfos = new HashSet<Object>();
    private Class associatedType;

    public AssociationBuilder()
    {
    }

    public AssociationBuilder isAggregate()
    {
        aggregate = true;
        return this;
    }

    public AssociationBuilder addChangeObserver( AssociationChangeObserver<?, ?> associationChangeObserver )
    {
        changeObservers.add( associationChangeObserver );
        return this;
    }

    public AssociationBuilder addAssociationInfo( Object info )
    {
        associationInfos.add( info );
        return this;
    }

    public <T> T withAccessor( Class<T> interfaceClass )
    {
        return interfaceClass.cast( Proxy.newProxyInstance( interfaceClass.getClassLoader(), new Class[]{ interfaceClass }, new AccessorInvocationHandler() ) );
    }

    class AccessorInvocationHandler
        implements InvocationHandler
    {
        public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
        {
            accessor = method;
            associatedType = (Class) ( (ParameterizedType) method.getGenericReturnType() ).getActualTypeArguments()[ 0 ];

            return method.getDefaultValue();
        }
    }
}
