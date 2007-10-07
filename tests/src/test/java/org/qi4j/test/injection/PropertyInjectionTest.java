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

import junit.framework.TestCase;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.PropertyValue;
import org.qi4j.api.annotation.Mixins;
import org.qi4j.api.annotation.scope.PropertyField;
import org.qi4j.api.annotation.scope.PropertyParameter;
import org.qi4j.runtime.CompositeBuilderFactoryImpl;

public class PropertyInjectionTest extends TestCase
{
    public void testPropertyFieldInjection()
    {
        CompositeBuilder<SayHelloComposite> builder = new CompositeBuilderFactoryImpl().newCompositeBuilder( SayHelloComposite.class );

        builder.properties( SampleInterface.class,
                            PropertyValue.property( "sampleOne", "Hello" ),
                            PropertyValue.property( "sampleTwo", "World" ) );

        SampleInterface sampleInterface = builder.newInstance();

        assertEquals( "Hello World", sampleInterface.say() );
    }

    @Mixins( SampleInterfaceMixin.class )
    public static interface SampleInterface
    {
        public String say();
    }

    public static abstract class AbstractSimpleInteface implements SampleInterface
    {
        @PropertyField protected String sampleOne;
    }

    public static class SampleInterfaceMixin extends AbstractSimpleInteface
    {
        private String sampleTwo;

        public SampleInterfaceMixin( @PropertyParameter( "sampleTwo" )String sampleTwo )
        {
            this.sampleTwo = sampleTwo;
        }

        public String say()
        {
            return sampleOne + " " + sampleTwo;
        }
    }

    public static interface SayHelloComposite extends SampleInterface, Composite
    {
    }
}

