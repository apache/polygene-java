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
 * TODO
 */
public final class MethodSideEffectsInstance
{
    private final Method method;
    private final List<InvocationHandler> sideEffects;
    private final SideEffectInvocationHandlerResult resultInvocationHandler;
    private final ProxyReferenceInvocationHandler proxyHandler;
    private final boolean hasSideEffects;

    public MethodSideEffectsInstance( Method method, List<InvocationHandler> sideEffects, SideEffectInvocationHandlerResult resultInvocationHandler, ProxyReferenceInvocationHandler proxyHandler )
    {
        this.method = method;
        this.sideEffects = sideEffects;
        this.resultInvocationHandler = resultInvocationHandler;
        this.proxyHandler = proxyHandler;
        this.hasSideEffects = !sideEffects.isEmpty();
    }

    public void invoke( Object proxy, Object[] params, Object result, Throwable throwable )
        throws Throwable
    {
        if( hasSideEffects )
        {
            proxyHandler.setProxy( proxy );
            resultInvocationHandler.setResult( result, throwable );

            try
            {
                for( InvocationHandler sideEffect : sideEffects )
                {
                    invokeSideEffect( proxy, params, throwable, sideEffect );
                }
            }
            finally
            {
                proxyHandler.clearProxy();
                resultInvocationHandler.setResult( null, null );
            }
        }
    }

    private void invokeSideEffect( Object proxy, Object[] params, Throwable originalThrowable, InvocationHandler sideEffect )
    {
        try
        {
            sideEffect.invoke( proxy, method, params );
        }
        catch( Throwable throwable )
        {
            if (throwable != originalThrowable)
                throwable.printStackTrace();
        }
    }
}