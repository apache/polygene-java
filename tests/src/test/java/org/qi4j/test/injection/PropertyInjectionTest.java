/*
 * Copyright (c) 2007, Lan Boon Ping. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.test.injection;

import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.PropertyField;
import org.qi4j.composite.scope.PropertyParameter;
import org.qi4j.property.Property;
import org.qi4j.test.AbstractQi4jTest;

public class PropertyInjectionTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( SayHelloComposite.class );
    }

    @Test
    public void testPropertyFieldInjection()
    {
        CompositeBuilder<SayHelloComposite> builder = compositeBuilderFactory.newCompositeBuilder( SayHelloComposite.class );

        SayHelloComposite state = builder.stateOfComposite();
        state.sampleOne().set( "Hello" );
        state.sampleTwo().set( "World" );

        SampleInterface sampleInterface = builder.newInstance();

        assertEquals( "Hello World", sampleInterface.say() );
    }

    @Mixins( { SampleInterfaceMixin.class } )
    public static interface SampleInterface
    {
        String say();

        Property<String> sampleOne();

        Property<String> sampleTwo();
    }

    public static abstract class AbstractSimpleInteface implements SampleInterface
    {
        @PropertyField protected Property<String> sampleOne;
    }

    public static class SampleInterfaceMixin extends AbstractSimpleInteface
    {
        private Property<String> sampleTwo;

        public SampleInterfaceMixin( @PropertyParameter( "sampleTwo" )Property<String> sampleTwo )
        {
            this.sampleTwo = sampleTwo;
        }

        public String say()
        {
            return sampleOne + " " + sampleTwo;
        }


        public Property<String> sampleOne()
        {
            return sampleOne;
        }

        public Property<String> sampleTwo()
        {
            return sampleTwo;
        }
    }

    public static interface SayHelloComposite extends SampleInterface, Composite
    {
    }
}

