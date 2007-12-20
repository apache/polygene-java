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
import java.util.HashMap;
import java.util.Map;
import org.qi4j.entity.property.PropertyChange;
import org.qi4j.entity.property.PropertyChangeObserver;
import org.qi4j.runtime.entity.property.NullPropertyContainer;
import org.qi4j.runtime.entity.property.PropertyInstance;
import org.qi4j.spi.structure.PropertyDescriptor;

/**
 * TODO
 */
public class PropertyBuilder
{
    private Class propertyType;
    private Map<Class, Object> propertyInfos = new HashMap<Class, Object>();
    private Object defaultValue;
    private Method accessor;

    public PropertyBuilder()
    {
    }

    public <T> T withAccessor( Class<T> interfaceClass )
    {
        return interfaceClass.cast( Proxy.newProxyInstance( interfaceClass.getClassLoader(), new Class[]{ interfaceClass }, new AccessorInvocationHandler() ) );
    }

    public PropertyBuilder addPropertyInfo( Object propertyInfo )
    {
        this.propertyInfos.put( propertyInfo.getClass(), propertyInfo );
        return this;
    }

    public PropertyDescriptor getPropertyDescriptor()
    {
        return new PropertyDescriptor( propertyType, propertyInfos, accessor, defaultValue );
    }

    class AccessorInvocationHandler
        implements InvocationHandler, PropertyChangeObserver<Object>
    {
        public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
        {
            accessor = method;
            propertyType = (Class) ( (ParameterizedType) method.getGenericReturnType() ).getActualTypeArguments()[ 0 ];

            PropertyInstance<Object> property = new PropertyInstance<Object>( new NullPropertyContainer<Object>(), null );
            property.addChangeObserver( this );
            return property;
        }

        public void onChange( PropertyChange<Object> propertyChange )
        {
            defaultValue = propertyChange.getNewValue();
        }
    }
}
