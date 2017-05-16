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

package org.apache.polygene.runtime.injection;

import org.junit.Test;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Test the @Uses annotation
 */
public class UsesInjectionTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.objects( InjectionTarget.class, ToBeInjected.class );
    }

    @Test
    public void givenUsedObjectWhenUseWithBuilderThenInjectReferences()
        throws Exception
    {
        ToBeInjected toBeInjected = new ToBeInjected();
        assertThat( "Injected object", objectFactory.newObject( InjectionTarget.class, toBeInjected, true )
            .getUsedObject(), is( equalTo( toBeInjected ) ) );
        assertThat( "Injected boolean", objectFactory.newObject( InjectionTarget.class, toBeInjected, true )
            .isUsedBoolean(), is( equalTo( true ) ) );
    }

    @Test
    public void givenUsedObjectBuilderWhenUseWithBuilderThenInjectNewInstance()
        throws Exception
    {
        assertThat( "Injected object", objectFactory.newObject( InjectionTarget.class, objectFactory.newObject( ToBeInjected.class ), true ), is( notNullValue() ) );
    }

    @Test
    public void givenNoUsesWhenBuilderNewInstanceThenInjectNewInstance()
        throws Exception
    {
        assertThat( "Injected object", objectFactory.newObject( InjectionTarget.class, true ), is( notNullValue() ) );
    }

    public static class InjectionTarget
    {
        @Uses
        ToBeInjected usedObject;

        @Uses
        boolean usedBoolean;

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
