/*
 * Copyright (c) 2008, Rickard …berg. All Rights Reserved.
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

package org.qi4j.structure;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.Structure;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Test of the Module interface.
 */
public class ModuleTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.setName( "Test" );
        module.addComposites( TestComposite.class );

    }

    @Test
    public void givenModuleWhenGetNameThenConfiguredNameIsReturned()
    {
        Module module = compositeBuilderFactory.newComposite( TestComposite.class ).getModule();
        assertThat( "module name is correct", module.name().get(), equalTo( "Test" ) );
    }

    @Test
    public void givenModuleWhenGetModuleForCompositeThenCorrectModuleIsReturned()
    {
        Module module = compositeBuilderFactory.newComposite( TestComposite.class ).getModule();
        assertThat( "module for composite is correct", module.moduleForComposite( TestComposite.class ), equalTo( module ) );
    }

    @Mixins( TestMixin.class )
    public interface TestComposite
        extends Composite
    {
        Module getModule();
    }

    public abstract static class TestMixin
        implements TestComposite
    {
        @Structure Module module;

        public Module getModule()
        {
            return module;
        }
    }
}
