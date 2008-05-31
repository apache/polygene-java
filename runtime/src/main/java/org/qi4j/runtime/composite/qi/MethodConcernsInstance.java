/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.composite.qi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.qi4j.runtime.composite.FragmentInvocationHandler;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;

/**
 * TODO
 */
public final class MethodConcernsInstance
{
    private Method method;
    private InvocationHandler firstConcern;
    private FragmentInvocationHandler mixinInvocationHandler;
    private ProxyReferenceInvocationHandler proxyHandler;

    public MethodConcernsInstance( Method method, InvocationHandler firstConcern, FragmentInvocationHandler mixinInvocationHandler, ProxyReferenceInvocationHandler proxyHandler )
    {
        this.method = method;
        this.firstConcern = firstConcern;
        this.mixinInvocationHandler = mixinInvocationHandler;
        this.proxyHandler = proxyHandler;
    }

    public Object invoke( Object proxy, Object[] params, Object mixin )
        throws Throwable
    {
        proxyHandler.setProxy( proxy );
        mixinInvocationHandler.setFragment( mixin );
        try
        {
            return firstConcern.invoke( proxy, method, params );
        }
        catch( InvocationTargetException e )
        {
            throw e.getTargetException();
        }
        finally
        {
            proxyHandler.clearProxy();
            mixinInvocationHandler.setFragment( null );
        }
    }
}
