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
 *
 *
 */
package org.apache.polygene.runtime.composite;

import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class UseCompositeTest extends AbstractPolygeneTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( Hello.class );
        module.objects( Speaker.class );
    }

    @Test
    public void givenCompositeToUsesWhenInstantiatingExpectException()
    {
        Hello hello = transientBuilderFactory.newTransient( Hello.class );
        hello.name().set( "World" );
        Speaker speaker = objectFactory.newObject( Speaker.class, hello );
        assertThat( speaker.speak(), equalTo( "Hello, World!" ) );
    }

    @Mixins( HelloMixin.class )
    public interface Hello
    {
        String sayHello();

        @Optional
        Property<String> name();
    }

    public static abstract class HelloMixin
        implements Hello
    {

        @Override
        public String sayHello()
        {
            return "Hello, " + name().get();
        }
    }

    public static class Speaker
    {
        @Uses
        Hello hello;

        String speak()
        {
            return hello.sayHello() + "!";
        }
    }
}
