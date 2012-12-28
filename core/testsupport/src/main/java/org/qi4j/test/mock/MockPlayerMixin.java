/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.test.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.injection.scope.This;
import org.qi4j.test.mock.internal.MockRepository;
import org.qi4j.test.mock.internal.MockResolver;

/**
 * Generic mixin for mock composites. Overrides any generic mixins but not typed mixins, as typed mixins have precedence
 * over generic mixins. To override a typed mixin {@link org.qi4j.test.mock.MockPlayerConcern} can be used.
 * MockResolver player mixin will delegate method invocations to registered mocks. Mocks can be registered by using
 * {@link org.qi4j.test.mock.MockComposite}.
 * If there is no mock registered to handle the method invocation invocation will fail by throwing an
 * IllegalStateException.
 */
public class MockPlayerMixin
    implements InvocationHandler
{

    /**
     * MockResolver repository. Holds all registred mocks.
     */
    @This
    MockRepository mockRepository;

    /**
     * Finds a registered mock that can handle the method invocation and delegate to it. If there is no such mock throws
     * IllegalStateException.
     *
     * @see java.lang.reflect.InvocationHandler#invoke(Object, java.lang.reflect.Method, Object[])
     */
    @Override
    public Object invoke( final Object proxy, final Method method, final Object[] args )
        throws Throwable
    {
        System.out.println( "Play mock for " + method );
        for( MockResolver mockResolver : mockRepository.getAll() )
        {
            InvocationHandler handler = mockResolver.getInvocationHandler( proxy, method, args );
            if( handler != null )
            {
                return handler.invoke( mockResolver, method, args );
            }
        }
        throw new IllegalStateException( "There is no mock registered that can handle " + method );
    }
}