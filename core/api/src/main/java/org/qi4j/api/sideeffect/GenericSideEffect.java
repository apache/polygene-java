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
package org.qi4j.api.sideeffect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Base class for generic SideEffects.
 */
public abstract class GenericSideEffect
    extends SideEffectOf<InvocationHandler>
    implements InvocationHandler
{

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke( final Object proxy, final Method method, final Object[] args )
        throws Throwable
    {
        invoke( method, args );
        return null;
    }

    /**
     * Convenience method to be overridden by subclasses in order to avoid returning null, as returned value from side
     * effects is not taken in consideration.
     *
     * @param method the method that was invoked
     * @param args   the arguments of the method invocation
     *
     * @throws Throwable - the exception to throw from the method invocation on the proxy instance. The exception's type
     *                   must be assignable either to any of the exception types declared in the throws clause of the
     *                   interface method or to the unchecked exception types {code}java.lang.RuntimeException{code}
     *                   or {code}java.lang.Error{code}. If a checked exception is thrown by this method that is not
     *                   assignable to any of the exception types declared in the throws clause of the interface method,
     *                   then an UndeclaredThrowableException containing the exception that was thrown by this method
     *                   will be thrown by the method invocation on the proxy instance.
     */
    protected void invoke( final Method method, final Object[] args )
        throws Throwable
    {
    }
}