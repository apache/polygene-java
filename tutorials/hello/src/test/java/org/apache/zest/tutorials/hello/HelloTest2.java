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
package org.apache.zest.tutorials.hello;

import org.junit.Test;
import org.apache.zest.api.composite.TransientBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class HelloTest2 extends AbstractZestTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( Hello.class );
    }

    @Test
    public void givenHelloValueInitializedToHelloWorldWhenCallingSayExpectHelloWorld()
    {
        TransientBuilder<Hello> builder = module.newTransientBuilder( Hello.class );
        builder.prototypeFor( Hello.State.class ).phrase().set( "Hello" );
        builder.prototypeFor( Hello.State.class ).name().set( "World" );
        Hello underTest = builder.newInstance();
        String result = underTest.say();
        assertThat( result, equalTo( "Hello World" ) );
    }
}
