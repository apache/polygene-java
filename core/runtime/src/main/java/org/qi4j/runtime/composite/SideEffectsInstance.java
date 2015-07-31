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
import java.lang.reflect.Method;
import java.util.List;

/**
 * JAVADOC
 */
public final class SideEffectsInstance
    implements InvocationHandler
{
    private final List<InvocationHandler> sideEffects;
    private final SideEffectInvocationHandlerResult resultInvocationHandler;
    private final ProxyReferenceInvocationHandler proxyHandler;
    private InvocationHandler invoker;

    public SideEffectsInstance( List<InvocationHandler> sideEffects,
                                SideEffectInvocationHandlerResult resultInvocationHandler,
                                ProxyReferenceInvocationHandler proxyHandler,
                                InvocationHandler invoker
    )
    {
        this.sideEffects = sideEffects;
        this.resultInvocationHandler = resultInvocationHandler;
        this.proxyHandler = proxyHandler;
        this.invoker = invoker;
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        try
        {
            Object result = invoker.invoke( proxy, method, args );
            invokeSideEffects( proxy, method, args, result, null );
            return result;
        }
        catch( Throwable throwable )
        {
            invokeSideEffects( proxy, method, args, null, throwable );
            throw throwable;
        }
    }

    private void invokeSideEffects( Object proxy,
                                    Method method,
                                    Object[] params,
                                    Object result,
                                    Throwable originalThrowable
    )
        throws Throwable
    {
        proxyHandler.setProxy( proxy );
        resultInvocationHandler.setResult( result, originalThrowable );

        try
        {
            for( InvocationHandler sideEffect : sideEffects )
            {
                try
                {
                    sideEffect.invoke( proxy, method, params );
                }
                catch( Throwable throwable )
                {
                    if( throwable != originalThrowable )
                    {
                        throwable.printStackTrace();
                    }
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