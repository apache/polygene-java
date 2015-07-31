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

/**
 * JAVADOC
 */
public final class CompositeMethodInstance
{
    private final InvocationHandler invoker;
    private final FragmentInvocationHandler mixinInvoker;
    private final Method method;
    private final int methodIdx;

    private CompositeMethodInstance next;

    public CompositeMethodInstance( InvocationHandler invoker,
                                    FragmentInvocationHandler mixinInvoker,
                                    Method method, int methodIdx
    )
    {
        this.invoker = invoker;
        this.method = method;
        this.mixinInvoker = mixinInvoker;
        this.methodIdx = methodIdx;
    }

    public Method method()
    {
        return method;
    }

    public Object getMixinFrom( Object[] mixins )
    {
        return mixins[ methodIdx ];
    }

    public Object invoke( Object composite, Object[] params, Object mixin )
        throws Throwable
    {
        mixinInvoker.setFragment( mixin );

        try
        {
            return invoker.invoke( composite, method, params );
        }
        finally
        {
            mixinInvoker.setFragment( null );
        }
    }

    public CompositeMethodInstance getNext()
    {
        return next;
    }

    public void setNext( CompositeMethodInstance next )
    {
        this.next = next;
    }
}
