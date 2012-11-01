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

package org.qi4j.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * JAVADOC
 */
public final class ConcernsInstance
    implements InvocationHandler
{
    private final InvocationHandler firstConcern;
    private final FragmentInvocationHandler mixinInvocationHandler;
    private final ProxyReferenceInvocationHandler proxyHandler;

    public ConcernsInstance( InvocationHandler firstConcern,
                             FragmentInvocationHandler mixinInvocationHandler,
                             ProxyReferenceInvocationHandler proxyHandler
    )
    {
        this.firstConcern = firstConcern;
        this.mixinInvocationHandler = mixinInvocationHandler;
        this.proxyHandler = proxyHandler;
    }

    public boolean isEmpty()
    {
        return firstConcern == mixinInvocationHandler;
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] params )
        throws Throwable
    {
        proxyHandler.setProxy( proxy );
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
        }
    }
}
