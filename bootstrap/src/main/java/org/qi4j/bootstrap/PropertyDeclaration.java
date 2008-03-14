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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.property.Property;
import org.qi4j.property.PropertyVetoException;
import org.qi4j.spi.structure.PropertyDescriptor;

/**
 * Declaration of a Property. Created by {@link ModuleAssembly#addProperty()}.
 */
public final class PropertyDeclaration
{
    private Class valueType;
    private Map<Class, Serializable> propertyInfos = new HashMap<Class, Serializable>();
    private Object defaultValue;
    private Method accessor;

    public PropertyDeclaration()
    {
    }

    public <T> T withAccessor( Class<T> mixinType )
    {
        return mixinType.cast( Proxy.newProxyInstance( mixinType.getClassLoader(), new Class[]{ mixinType }, new AccessorInvocationHandler() ) );
    }

    public <T extends Serializable> PropertyDeclaration setPropertyInfo( Class<T> infoType, T propertyInfo )
    {
        this.propertyInfos.put( infoType, propertyInfo );
        return this;
    }

    public PropertyDescriptor getPropertyDescriptor()
    {
        return new PropertyDescriptor( valueType, propertyInfos, accessor, defaultValue );
    }

    class AccessorInvocationHandler
        implements InvocationHandler
    {
        public Object invoke( Object object, Method method, Object[] objects )
            throws Throwable
        {
            accessor = method;
            Type methodReturnType = method.getGenericReturnType();
            valueType = getPropertyType( methodReturnType );

            return Proxy.newProxyInstance( method.getReturnType().getClassLoader(), new Class[]{ method.getReturnType() }, new PropertyInvocationHandler() );
        }

        private Class getPropertyType( Type methodReturnType )
        {
            if( methodReturnType instanceof ParameterizedType )
            {
                ParameterizedType parameterizedType = (ParameterizedType) methodReturnType;
                if( Property.class.isAssignableFrom( (Class<?>) parameterizedType.getRawType() ) )
                {
                    return (Class) parameterizedType.getActualTypeArguments()[ 0 ];
                }
            }

            Type[] interfaces = ( (Class) methodReturnType ).getInterfaces();
            for( Type anInterface : interfaces )
            {
                Class propertyType = getPropertyType( anInterface );
                if( propertyType != null )
                {
                    return propertyType;
                }
            }
            return null;
        }
    }

    class PropertyInvocationHandler
        implements InvocationHandler, Property<Object>
    {
        public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
        {
            return method.invoke( this, objects );
        }

        public Object get()
        {
            return null;
        }

        public <T> T getPropertyInfo( Class<T> infoType )
        {
            return null;
        }

        public String getName()
        {
            return null;
        }

        public String getQualifiedName()
        {
            return null;
        }

        public Object set( Object newValue ) throws PropertyVetoException
        {
            defaultValue = newValue;
            return newValue;
        }
    }
}
