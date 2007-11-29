/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.test.model2;

import org.qi4j.runtime.CompositeModelFactory;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.test.AbstractQi4jTest;

public class Composite2Test extends AbstractQi4jTest
{
    CompositeModelFactory modelFactory;
    CompositeModel composite;

    protected void setUp() throws Exception
    {
        modelFactory = new CompositeModelFactory();
        composite = modelFactory.newCompositeModel( TestComposite.class );
    }

    public void testGetImplementation() throws Exception
    {
        assertEquals( 0, composite.getImplementations( Standard.class ).size() );

        System.out.println( composite );

        assertEquals( DomainInterfaceImpl.class, composite.getImplementations( DomainInterface.class ).get( 0 ).getModelClass() );
        assertEquals( StandardThisImpl.class, composite.getImplementations( StandardThis.class ).get( 0 ).getModelClass() );
        assertEquals( StandardThatImpl.class, composite.getImplementations( StandardThat.class ).get( 0 ).getModelClass() );

        {
            TestComposite object = compositeBuilderFactory.newCompositeBuilder( TestComposite.class ).newInstance();

            assertEquals( "bar=foo:FOO Hello World", object.foo( "FOO " ) );

            object.setFoo( "xyz" );
            try
            {
                object.setFoo( null );
                fail( "Should have thrown an exception" );
            }
            catch( Exception e )
            {
                // Ok
            }
        }

        {
            TestComposite object = compositeBuilderFactory.newCompositeBuilder( CustomTestComposite.class ).newInstance();

            object.setFoo( "xyz" );
            System.out.println( object );
            assertEquals( "FOO:foo:xyz", object.getFoo() );
            object.setFoo( null );
        }
    }
}