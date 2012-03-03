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

import java.util.ArrayList;
import java.util.Collection;
import org.qi4j.test.mock.MockRecorder;
import org.qi4j.test.mock.MockResolverType;

public class MockRecorderMixin
    implements MockRecorder, MockRepository
{

    private final Collection<MockResolver> mockResolvers;

    public MockRecorderMixin()
    {
        this.mockResolvers = new ArrayList<MockResolver>();
    }

    public MockResolverType useMock( Object mock )
    {
        System.out.println( "Recorded " + mock );
        MockResolverProxy proxy = new MockResolverProxy( mock, new UnresolvableMockResolver() );
        add( proxy );
        return new MockResolverTypeImpl( proxy );
    }

    public void add( MockResolver mockResolver )
    {
        mockResolvers.add( mockResolver );
    }

    public Iterable<MockResolver> getAll()
    {
        return mockResolvers;
    }
}