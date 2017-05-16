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
package org.apache.polygene.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import org.apache.polygene.api.composite.CompositeInvoker;

public final class ProxyReferenceInvocationHandler
    implements InvocationHandler, CompositeInvoker
{
    private Object proxy;

    public Object proxy()
    {
        return proxy;
    }

    public void setProxy( Object proxy )
    {
        this.proxy = proxy;
    }

    public void clearProxy()
    {
        proxy = null;
    }

    @Override
    public Object invokeComposite( Method method, Object[] args )
        throws Throwable
    {
        try
        {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler( this.proxy );
            return invocationHandler.invoke( this.proxy, method, args );
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

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        try
        {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler( this.proxy );
            return invocationHandler.invoke( this.proxy, method, args );
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
}
