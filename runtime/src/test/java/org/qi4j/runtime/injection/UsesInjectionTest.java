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

package org.qi4j.runtime.injection;

import org.junit.Test;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

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

    @Test
    public void givenUsedObjectBuilderWhenUseWithBuilderThenInjectNewInstance()
        throws Exception
    {
        ObjectBuilder<InjectionTarget> builder = objectBuilderFactory.newObjectBuilder( InjectionTarget.class );
        builder.use( objectBuilderFactory.newObjectBuilder( ToBeInjected.class ) );
        builder.use( true );
        assertThat( "Injected object", builder.newInstance().getUsedObject(), is( notNullValue() ) );
    }

    @Test
    public void givenNoUsesWhenBuilderNewInstanceThenInjectNewInstance()
        throws Exception
    {
        ObjectBuilder<InjectionTarget> builder = objectBuilderFactory.newObjectBuilder( InjectionTarget.class );
        builder.use( true );
        assertThat( "Injected object", builder.newInstance().getUsedObject(), is( notNullValue() ) );

        InjectionTarget target = builder.newInstance();
        ToBeInjected instance1 = target.newUsedObject();
        ToBeInjected instance2 = target.newUsedObject();
        assertThat( "New object", instance1, not( equalTo( instance2 ) ) );
    }

    public static class InjectionTarget
    {
        @Uses
        Iterable<ToBeInjected> usedObjects;

        @Uses
        ToBeInjected usedObject;

        @Uses
        boolean usedBoolean;

        public ToBeInjected getUsedObject()
        {
            return usedObject;
        }

        public ToBeInjected newUsedObject()
        {
            return usedObjects.iterator().next();
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
