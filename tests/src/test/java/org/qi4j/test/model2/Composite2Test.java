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

import junit.framework.TestCase;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembly;
import org.qi4j.spi.composite.CompositeModel;

public class Composite2Test extends TestCase
{
    public void testGetImplementation() throws Exception
    {
        SingletonAssembly assembly = new SingletonAssembly()
        {
            public void configure( ModuleAssembly module ) throws AssemblyException
            {
                module.addComposite( TestComposite.class );
            }
        };

        CompositeModel composite = assembly.getModuleInstance().getModuleContext().getCompositeContext( TestComposite.class ).getCompositeBinding().getCompositeResolution().getCompositeModel();

        assertEquals( 0, composite.getImplementations( Standard.class ).size() );

        System.out.println( composite );

        assertEquals( DomainInterfaceImpl.class, composite.getImplementations( DomainInterface.class ).get( 0 ).getModelClass() );
        assertEquals( StandardThisImpl.class, composite.getImplementations( StandardThis.class ).get( 0 ).getModelClass() );
        assertEquals( StandardThatImpl.class, composite.getImplementations( StandardThat.class ).get( 0 ).getModelClass() );

        {
            TestComposite object = assembly.getCompositeBuilderFactory().newCompositeBuilder( TestComposite.class ).newInstance();

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
    }

    public void testCustomizedImplementation() throws Exception
    {
        SingletonAssembly assembly = new SingletonAssembly()
        {
            public void configure( ModuleAssembly module ) throws AssemblyException
            {
                module.addComposite( CustomTestComposite.class );
            }
        };

        {
            TestComposite object = assembly.getCompositeBuilderFactory().newCompositeBuilder( TestComposite.class ).newInstance();

            object.setFoo( "xyz" );
            System.out.println( object );
            assertEquals( "FOO:foo:xyz", object.getFoo() );
            object.setFoo( null );
        }
    }
}