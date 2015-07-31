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
package org.qi4j.test.mock.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.util.NullArgumentException;

/**
 * Proxy to another mock resolver that can be set/changed over time. This allows
 * {@link org.qi4j.test.mock.MockResolverType} to change the mock resolver for a mock.
 */
public class MockResolverProxy
    implements MockResolver
{
    /**
     * Registered mock. Cannot be null.
     */
    private final Object registeredMock;
    /**
     * Mock resolver delegate. Cannot be null.
     */
    private MockResolver mockResolver;

    /**
     * Constructor.
     *
     * @param registeredMock registered mock; cannot be null
     * @param mockResolver   mock resolver delegate; cannot be null
     *
     * @throws NullArgumentException - If registred mock is null
     *                               - If mock resolver is null
     */
    MockResolverProxy( final Object registeredMock, final MockResolver mockResolver )
    {
        NullArgumentException.validateNotNull( "Registered mock", registeredMock );
        NullArgumentException.validateNotNull( "Mock resolver delegate", mockResolver );
        this.registeredMock = registeredMock;
        this.mockResolver = mockResolver;
    }

    /**
     * Setter.
     *
     * @param mockResolver mock resolver delegate; cannot be null
     *
     * @return itself
     *
     * @throws NullArgumentException - If mock resolver is null
     */
    MockResolverProxy setMock( final MockResolver mockResolver )
    {
        NullArgumentException.validateNotNull( "Mock resolver delegate", mockResolver );
        this.mockResolver = mockResolver;
        return this;
    }

    /**
     * Getter.
     *
     * @return registered mock
     */
    public Object getRegisteredMock()
    {
        return registeredMock;
    }

    /**
     * Delegates to current mock resolver delegate.
     *
     * @see MockResolver#getInvocationHandler(Object, java.lang.reflect.Method, Object[])
     */
    @Override
    public InvocationHandler getInvocationHandler( final Object proxy, final Method method, final Object[] args )
    {
        return mockResolver.getInvocationHandler( proxy, method, args );
    }
}