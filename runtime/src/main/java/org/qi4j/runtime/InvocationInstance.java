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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.qi4j.api.Composite;

public final class InvocationInstance<T extends Composite>
{
    private Object firstModifier;
    private Method method;
    private Class mixinType;
    private FragmentInvocationHandler mixinInvocationHandler;
    private ProxyReferenceInvocationHandler<T> proxyHandler;
    private InvocationInstancePool pool;
    private InvocationInstance next;

    public InvocationInstance( Object aFirstModifier, FragmentInvocationHandler aMixinInvocationHandler, ProxyReferenceInvocationHandler aProxyHandler, InvocationInstancePool aPool, Method method, Class mixinType )
    {
        this.mixinType = mixinType;
        this.method = method;
        firstModifier = aFirstModifier;
        proxyHandler = aProxyHandler;
        mixinInvocationHandler = aMixinInvocationHandler;
        pool = aPool;
    }

    public Object invoke( T proxy, Object[] args, Object mixin )
        throws Throwable
    {
        pool.returnInstance( this );

        try
        {
            if( firstModifier == null )
            {
                if( mixin instanceof InvocationHandler )
                {
                    return ( (InvocationHandler) mixin ).invoke( proxy, method, args );
                }
                else
                {
                    return method.invoke( mixin, args );
                }
            }
            else
            {
                proxyHandler.setContext( proxy, mixin, mixinType );
                mixinInvocationHandler.setFragment( mixin );
                if( firstModifier instanceof InvocationHandler )
                {
                    return ( (InvocationHandler) firstModifier ).invoke( proxy, method, args );
                }
                else
                {
                    return method.invoke( firstModifier, args );
                }
            }
        }
        catch( InvocationTargetException e )
        {
            throw e.getTargetException();
        }
        finally
        {
            pool.returnInstance( this );
        }
    }

    public InvocationInstance getNext()
    {
        return next;
    }

    public void setNext( InvocationInstance next )
    {
        this.next = next;
    }
}
