/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.polygene.runtime.mixin;

import org.apache.polygene.api.composite.TransientBuilder;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Initial tests for interface default methods support.
 */
public class DefaultMethodsTest extends AbstractPolygeneTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( Hello.class );
        module.transients( Hello.class ).withMixins( SpeakMixin.class );
    }

    @Test
    public void givenInterfaceWithDefaultMethodWhenCallingExpectSuccess()
    {
        ValueBuilder<Hello> builder = valueBuilderFactory.newValueBuilder( Hello.class );
        Hello prototype = builder.prototype();
        Property<String> phrase = prototype.phrase();
        phrase.set( "Hello" );
        Hello hello = builder.newInstance();
        assertThat( hello.speak(), equalTo( "Hello" ) );
    }

    @Test
    public void givenInterfaceWithDefaultMethodAndMixinImplementationWhenCallingExpectMixinValueReturned()
    {
        TransientBuilder<Hello> builder = transientBuilderFactory.newTransientBuilder( Hello.class );
        Hello prototype = builder.prototype();
        Property<String> phrase = prototype.phrase();
        phrase.set( "Hello" );
        Hello hello = builder.newInstance();
        assertThat( hello.speak(), equalTo( "Hello, Mixin!" ) );
    }

    public interface Hello
    {
        Property<String> phrase();

        default String speak()
        {
            return phrase().get();
        }
    }

    public static abstract class SpeakMixin
        implements Hello
    {
        @Override
        public String speak()
        {
            return phrase().get() + ", Mixin!";
        }
    }

}
