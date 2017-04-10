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
package org.apache.polygene.runtime.concerns;

import org.apache.polygene.api.concern.ConcernOf;
import org.apache.polygene.api.concern.Concerns;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.sideeffect.SideEffectOf;
import org.apache.polygene.api.sideeffect.SideEffects;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class MethodConcernsTest extends AbstractPolygeneTest
{
    private static int count = 0;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( SomeType.class );
    }

    @Test
    public void givenTypedConcernWhenCallingMethodExpectConcernToBeInvoked()
        throws Exception
    {
        SomeType value = valueBuilderFactory.newValue( SomeType.class );
        assertThat( value.doSomething( "abc" ), equalTo( "(...abc...)" ) );
        assertThat( count, equalTo(1) );
    }

    @Mixins( Mixin.class )
    public interface SomeType
    {
        @Concerns( ParenWrapConcern.class )
        @SideEffects( CountInvocationsSideEffect.class )
        String doSomething( String value );
    }

    public class Mixin implements SomeType
    {

        @Override
        public String doSomething( String value )
        {
            return "..." + value + "...";
        }
    }

    public static class ParenWrapConcern extends ConcernOf<SomeType>
        implements SomeType
    {

        @Override
        public String doSomething( String value )
        {
            return "(" + next.doSomething( value ) + ")";
        }
    }

    public static class CountInvocationsSideEffect extends SideEffectOf<SomeType>
        implements SomeType
    {
        @Override
        public String doSomething( String value )
        {
            count++;
            return null;  // side effect returns are ignored.
        }
    }
}
