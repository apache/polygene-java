/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.runtime;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import org.qi4j.api.InvocationContext;
import org.qi4j.api.Composite;

public final class ProxyReferenceInvocationHandler<T extends Composite>
    implements InvocationHandler, InvocationContext
{
    private T proxy;
    private Object mixin;
    private Class mixinType;

    public T getProxy()
    {
        return proxy;
    }

    public Object getMixin()
    {
        return mixin;
    }

    public Class getMixinType()
    {
        return mixinType;
    }

    public void setContext( T aProxy, Object aMixin, Class mixinType )
    {
        this.mixinType = mixinType;
        proxy = aProxy;
        mixin = aMixin;
    }

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        try
        {
            return method.invoke( this.proxy, args );
        }
        catch( InvocationTargetException e )
        {
            throw e.getTargetException();
        }
        catch( UndeclaredThrowableException e )
        {
            throw e.getUndeclaredThrowable();
        }
    }

    public void initializeMixins( Map<Class, Object> mixins )
        throws IntrospectionException, IllegalAccessException, InvocationTargetException
    {
        // TODO Improve?
        Object currentMixin = mixins.get( getMixinType() );
        Object invokedMixin = getMixin();
        BeanInfo info = Introspector.getBeanInfo( currentMixin.getClass() );
        PropertyDescriptor[] properties = info.getPropertyDescriptors();
        for( PropertyDescriptor property : properties )
        {
            Method read = property.getReadMethod();
            Method write = property.getWriteMethod();
            if( read != null && write != null )
            {
                Object value = property.getReadMethod().invoke( currentMixin, new Object[0] );
                Method writeMethod = property.getWriteMethod();
                writeMethod.invoke( invokedMixin, value );
            }
        }
    }
}
