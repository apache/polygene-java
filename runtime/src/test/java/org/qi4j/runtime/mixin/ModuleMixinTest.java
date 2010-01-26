/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.mixin;

import org.junit.Test;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Test of declaring mixin in assembly
 */
public class ModuleMixinTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( FooComposite.class ).withMixins( CustomFooMixin.class );
    }

    @Test
    public void testModuleMixins()
    {
        assertThat( "Custom mixin has executed", transientBuilderFactory.newTransient( Foo.class ).test( "Foo", 42 ), equalTo( "Foo/42" ) );
    }

    public interface FooComposite
        extends TransientComposite, Foo
    {
    }

    public interface Foo
    {
        String test( String foo, int bar );
    }

    public static class FooMixin
        implements Foo
    {
        public String test( String foo, int bar )
        {
            return foo + " " + bar;
        }
    }

    public static class CustomFooMixin
        implements Foo
    {
        public String test( String foo, int bar )
        {
            return foo + "/" + bar;
        }
    }
}