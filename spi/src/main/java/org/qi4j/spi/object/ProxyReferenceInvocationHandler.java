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
package org.qi4j.spi.object;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import org.qi4j.api.InvocationContext;

public final class ProxyReferenceInvocationHandler
    implements InvocationHandler, InvocationContext
{
    private Object proxy;
    private Object mixin;
    private Class mixinType;

    public Object getProxy()
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

    public void setContext( Object aProxy , Object aMixin, Class aMixinType )
    {
        proxy = aProxy;
        mixin = aMixin;
        mixinType = aMixinType;
    }

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        try
        {
            return method.invoke( this.proxy, args);
        }
        catch( InvocationTargetException e )
        {
            throw e.getTargetException();
        }
        catch ( UndeclaredThrowableException e)
        {
            throw e.getUndeclaredThrowable();
        }
    }
}
