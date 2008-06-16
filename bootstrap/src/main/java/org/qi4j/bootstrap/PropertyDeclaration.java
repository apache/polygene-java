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
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import org.qi4j.property.Property;
import org.qi4j.util.MetaInfo;

/**
 * Declaration of a Property. Created by {@link ModuleAssembly#addProperty()}.
 */
public final class PropertyDeclaration
{
    MetaInfo metaInfo = new MetaInfo();
    Object defaultValue;
    Method accessor;

    public PropertyDeclaration()
    {
    }

    public <T> T withAccessor( Class<T> mixinType )
    {
        return mixinType.cast( Proxy.newProxyInstance( mixinType.getClassLoader(), new Class[]{ mixinType }, new AccessorInvocationHandler() ) );
    }

    public <T extends Serializable> PropertyDeclaration setMetaInfo( Serializable info )
    {
        metaInfo.set( info );
        return this;
    }

    class AccessorInvocationHandler
        implements InvocationHandler
    {
        public Object invoke( Object object, Method method, Object[] objects )
            throws Throwable
        {
            accessor = method;
            Type methodReturnType = method.getGenericReturnType();

            return Proxy.newProxyInstance( method.getReturnType().getClassLoader(), new Class[]{ method.getReturnType() }, new PropertyInvocationHandler() );
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

        public <T> T metaInfo( Class<T> infoType )
        {
            return null;
        }

        public String name()
        {
            return null;
        }

        public String qualifiedName()
        {
            return null;
        }

        public Type type()
        {
            return null;
        }

        public void set( Object newValue ) throws IllegalArgumentException
        {
            defaultValue = newValue;
        }
    }
}
