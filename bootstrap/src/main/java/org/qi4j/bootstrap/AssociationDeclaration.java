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

package org.qi4j.bootstrap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.spi.structure.AssociationDescriptor;

/**
 * TODO
 */
public final class AssociationDeclaration
{
    private Method accessor;
    private Map<Class, Object> associationInfos = new HashMap<Class, Object>();
    private Class associatedType;
    private Class associationType;

    public AssociationDeclaration()
    {
    }

    public AssociationDeclaration addAssociationInfo( Object info )
    {
        associationInfos.put( info.getClass(), info );
        return this;
    }

    public <T> T withAccessor( Class<T> interfaceClass )
    {
        return interfaceClass.cast( Proxy.newProxyInstance( interfaceClass.getClassLoader(), new Class[]{ interfaceClass }, new AccessorInvocationHandler() ) );
    }

    public AssociationDescriptor getAssociationDescriptor()
    {
        return new AssociationDescriptor( associationType, associatedType, associationInfos, accessor );
    }

    class AccessorInvocationHandler
        implements InvocationHandler
    {
        public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
        {
            accessor = method;
            Type methodReturnType = method.getGenericReturnType();
            associatedType = getAssociatedType( methodReturnType );
            associationType = getAssociationType( methodReturnType );

            return method.getDefaultValue();
        }

        private Class getAssociatedType( Type methodReturnType )
        {
            if( methodReturnType instanceof ParameterizedType )
            {
                ParameterizedType parameterizedType = (ParameterizedType) methodReturnType;
                if( AbstractAssociation.class.isAssignableFrom( (Class<?>) parameterizedType.getRawType() ) )
                {
                    return (Class) parameterizedType.getActualTypeArguments()[ 0 ];
                }
            }

            Type[] interfaces = ( (Class) methodReturnType ).getInterfaces();
            for( Type anInterface : interfaces )
            {
                Class associationType = getAssociatedType( anInterface );
                if( associationType != null )
                {
                    return associationType;
                }
            }
            return null;
        }

        private Class getAssociationType( Type methodReturnType )
        {
            if( methodReturnType instanceof ParameterizedType )
            {
                ParameterizedType parameterizedType = (ParameterizedType) methodReturnType;
                if( AbstractAssociation.class.isAssignableFrom( (Class<?>) parameterizedType.getRawType() ) )
                {
                    return (Class) parameterizedType.getRawType();
                }
            }

            Type[] interfaces = ( (Class) methodReturnType ).getInterfaces();
            for( Type anInterface : interfaces )
            {
                Class associationType = getAssociationType( anInterface );
                if( associationType != null )
                {
                    return associationType;
                }
            }
            return null;
        }
    }
}
