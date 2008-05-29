/*
 * Copyright (c) 2008, Rickard …berg. All Rights Reserved.
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

import java.lang.reflect.Method;

/**
 * TODO
 */
public final class CompositeMethodInstance
{
    private ConcernsInstance concerns;
    private SideEffectsInstance sideEffects;
    private Method method;

    public CompositeMethodInstance( ConcernsInstance concerns, SideEffectsInstance sideEffects, Method method )
    {
        this.concerns = concerns;
        this.sideEffects = sideEffects;
        this.method = method;
    }

    public Method method()
    {
        return method;
    }

    public Object invoke( Object composite, Object[] params, Object mixin )
        throws Throwable
    {
        try
        {
            Object result = concerns.invoke( composite, params, mixin );
            sideEffects.invoke( composite, params, result, null );
            return result;
        }
        catch( Throwable throwable )
        {
            sideEffects.invoke( composite, params, null, throwable );
            throw throwable;
        }
    }

}
