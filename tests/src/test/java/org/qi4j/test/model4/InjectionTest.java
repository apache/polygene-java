/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.test.model4;

import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.test.AbstractQi4jTest;

public class InjectionTest extends AbstractQi4jTest
{
    public void configure( ModuleAssembly module )
    {
        module.addComposites( Composite1.class );
    }

    public void testMixin()
        throws Exception
    {
        CompositeBuilder<Composite1> builder = compositeBuilderFactory.newCompositeBuilder( Composite1.class );

        Composite1 instance = builder.newInstance();
        assertNotNull( "DependencyOld not injected.", instance.getBuilderFactory() );
        assertNotNull( "ThisCompositeAs not injected.", instance.getMeAsMixin2() );
        assertEquals( 1, instance.getValue() );
        instance.getBuilderFactory();
        instance.getBuilderFactory();
        instance.getBuilderFactory();
        assertEquals( 4, instance.getValue() );
    }
}
