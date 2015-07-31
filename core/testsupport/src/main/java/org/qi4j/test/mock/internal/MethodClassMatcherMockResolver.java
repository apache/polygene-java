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

public class MethodClassMatcherMockResolver
    implements MockResolver, InvocationHandler
{

    private final Object recordedMock;
    private final Class methodClass;

    public MethodClassMatcherMockResolver( Object recordedMock, Class methodClass )
    {
        this.recordedMock = recordedMock;
        this.methodClass = methodClass;
    }

    @Override
    public InvocationHandler getInvocationHandler( Object proxy, Method method, Object[] args )
    {
        if( method.getDeclaringClass().equals( methodClass ) )
        {
            return this;
        }
        return null;
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        return method.invoke( recordedMock, args );
    }
}