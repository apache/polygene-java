/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.tutorials.hello;

import org.junit.Test;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class HelloTest3 extends AbstractQi4jTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( Hello.class );
        module.forMixin( Hello.State.class ).declareDefaults().phrase().set( "Hello" );
        module.forMixin( Hello.State.class ).declareDefaults().name().set( "World" );
    }

    @Test
    public void givenHelloValueInitializedToHelloWorldWhenCallingSayExpectHelloWorld()
    {
        ServiceReference<Hello> service = module.findService( Hello.class );
        String result = service.get().say();
        assertThat( result, equalTo( "Hello World" ) );
    }
}
