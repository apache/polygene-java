/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.composite.scope;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Test the @Uses annotation
 */
public class UsesInjectionTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addObjects( InjectionTarget.class, ToBeInjected.class );
    }

    /**
     * Tests the injected object for {@link @org.qi4j.composite.scope.Uses} annotation.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void givenUsedObjectWhenUseWithBuilderThenInjectReferences()
        throws Exception
    {
        ObjectBuilder<InjectionTarget> builder = objectBuilderFactory.newObjectBuilder( InjectionTarget.class );
        ToBeInjected toBeInjected = new ToBeInjected();
        builder.use( toBeInjected );
        builder.use( true );
        assertThat( "Injected object", builder.newInstance().getUsedObject(), is( equalTo( toBeInjected ) ) );
        assertThat( "Injected boolean", builder.newInstance().isUsedBoolean(), is( equalTo( true ) ) );
    }

    public static class InjectionTarget
    {
        @Uses ToBeInjected usedObject;
        @Uses boolean usedBoolean;

        public ToBeInjected getUsedObject()
        {
            return usedObject;
        }

        public boolean isUsedBoolean()
        {
            return usedBoolean;
        }
    }

    public static class ToBeInjected
    {
    }

}
