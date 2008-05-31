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
import java.lang.reflect.Method;
import java.util.List;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.composite.SideEffectInvocationHandlerResult;

/**
 * TODO
 */
public class MethodSideEffectsInstance
{
    private Method method;
    private List<InvocationHandler> sideEffects;
    private SideEffectInvocationHandlerResult resultInvocationHandler;
    private ProxyReferenceInvocationHandler proxyHandler;

    public MethodSideEffectsInstance( Method method, List<InvocationHandler> sideEffects, SideEffectInvocationHandlerResult resultInvocationHandler, ProxyReferenceInvocationHandler proxyHandler )
    {
        this.method = method;
        this.sideEffects = sideEffects;
        this.resultInvocationHandler = resultInvocationHandler;
        this.proxyHandler = proxyHandler;
    }

    public void invoke( Object proxy, Object[] params, Object result, Throwable throwable )
        throws Throwable
    {
        proxyHandler.setProxy( proxy );
        resultInvocationHandler.setResult( result, throwable );

        try
        {
            for( InvocationHandler sideEffect : sideEffects )
            {
                try
                {
                    sideEffect.invoke( proxy, method, params );
                }
                catch( Throwable throwable1 )
                {
                    throwable1.printStackTrace();
                }
            }
        }
        finally
        {
            proxyHandler.clearProxy();
            resultInvocationHandler.setResult( null, null );
        }
    }
}