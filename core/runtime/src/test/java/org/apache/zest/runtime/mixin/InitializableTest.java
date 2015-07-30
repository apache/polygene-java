/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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

package org.apache.zest.runtime.mixin;

import org.junit.Test;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.mixin.Initializable;
import org.apache.zest.api.mixin.InitializationException;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of Initializable interface
 */
public class InitializableTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.objects( TestObject.class );
        module.transients( TestComposite.class );
    }

    @Test
    public void givenCompositeWithInitializableMixinWhenInstantiatedThenInvokeInitialize()
    {
        TestComposite instance = module.newTransient( TestComposite.class );
        assertThat( "mixin has been initialized", instance.ok(), equalTo( true ) );
    }

    @Test
    public void givenObjectImplementingInitializableWhenInstantiatedThenInvokeInitialize()
    {
        TestObject instance = module.newObject( TestObject.class );
        assertThat( "object has been initialized", instance.ok(), equalTo( true ) );
    }

    @Mixins( TestMixin.class )
    public interface TestComposite
        extends TransientComposite
    {
        boolean ok();
    }

    public abstract static class TestMixin
        implements TestComposite, Initializable
    {
        boolean ok = false;

        public void initialize()
            throws InitializationException
        {
            ok = true;
        }

        public boolean ok()
        {
            return ok;
        }
    }

    public static class TestObject
        implements Initializable
    {
        boolean ok = false;

        public void initialize()
            throws InitializationException
        {
            ok = true;
        }

        public boolean ok()
        {
            return ok;
        }
    }
}
