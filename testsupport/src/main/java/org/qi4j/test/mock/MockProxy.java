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

public class MockProxy
    implements Mock
{
    private final Object recordedMock;
    private Mock mock;

    MockProxy( Object recordedMock, Mock mock )
    {
        this.recordedMock = recordedMock;
        this.mock = mock;
    }

    MockProxy setMock( Mock mock )
    {
        this.mock = mock;
        return this;
    }

    public Object getRecordedMock()
    {
        return recordedMock;
    }

    public InvocationHandler getInvocationHandler( Object proxy, Method method, Object[] args )
    {
        return mock.getInvocationHandler( proxy, method, args );
    }
}