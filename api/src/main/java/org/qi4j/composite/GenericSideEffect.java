/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Base class for generic SideEffects.
 */
public abstract class GenericSideEffect
    extends SideEffectOf<InvocationHandler>
    implements InvocationHandler
{
    public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
    {
        invoke( method, objects );
        return null;
    }

    /**
     * Override this method to avoid having to do "return null;" in the code
     *
     * @param method  the method that was invoked
     * @param objects the arguments of the method invocation
     */
    protected void invoke( Method method, Object[] objects )
    {
    }
}