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
package org.apache.polygene.bootstrap;

import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.mixin.NoopMixin;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@Ignore
public class RuntimeMixinsTest
{
    @Test
    public void givenValueWithRuntimeMixinsWhenAssembledExpectCorrectComposition()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.values( TestType1.class ).withMixins( DoThisMixin.class, DoThatMixin.class );
            }
        };
        TestType1 value = singletonAssembler.valueBuilderFactory().newValue( TestType1.class );
        assertThat( value.doThis(), equalTo( "this" ) );
        assertThat( value.doThat(), equalTo( "that" ) );
    }

    @Test
    public void givenValueWithRuntimeMixinOverrideWhenAssembledExpectCorrectComposition()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.values( TestType2.class ).withMixins( DoThisMixin.class );
            }
        };
        TestType2 value = singletonAssembler.valueBuilderFactory().newValue( TestType2.class );
        assertThat( value.doThis(), equalTo( "this" ) );
        assertThat( value.doThat(), nullValue() );
    }

    @Test
    public void givenTransientWithRuntimeMixinsWhenAssembledExpectCorrectComposition()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( TestType1.class ).withMixins( DoThisMixin.class, DoThatMixin.class );
            }
        };
        TestType1 value = singletonAssembler.transientBuilderFactory().newTransient( TestType1.class );
        assertThat( value.doThis(), equalTo( "this" ) );
        assertThat( value.doThat(), equalTo( "that" ) );
    }

    @Test
    public void givenTransientWithRuntimeMixinOverrideWhenAssembledExpectCorrectComposition()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( TestType2.class ).withMixins( DoThisMixin.class );
            }
        };
        TestType2 value = singletonAssembler.transientBuilderFactory().newTransient( TestType2.class );
        assertThat( value.doThis(), equalTo( "this" ) );
        assertThat( value.doThat(), nullValue() );
    }

    @Test
    public void givenServiceWithRuntimeMixinsWhenAssembledExpectCorrectComposition()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.services( TestType1.class ).withMixins( DoThisMixin.class, DoThatMixin.class );
            }
        };
        TestType1 value = singletonAssembler.serviceFinder().findService( TestType1.class ).get();
        assertThat( value.doThis(), equalTo( "this" ) );
        assertThat( value.doThat(), equalTo( "that" ) );
    }

    @Test
    public void givenServiceWithRuntimeMixinOverrideWhenAssembledExpectCorrectComposition()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.services( TestType2.class ).withMixins( DoThisMixin.class );
            }
        };
        TestType2 value = singletonAssembler.serviceFinder().findService( TestType2.class ).get();
        assertThat( value.doThis(), equalTo( "this" ) );
        assertThat( value.doThat(), nullValue() );
    }

    public interface TestType1
    {
        String doThis();

        String doThat();
    }

    @Mixins( NoopMixin.class )
    public interface TestType2
    {
        String doThis();

        String doThat();
    }

    protected abstract static class DoThisMixin
        implements TestType1
    {
        @Override
        public String doThis()
        {
            return "this";
        }
    }

    protected abstract static class DoThatMixin
        implements TestType1
    {
        @Override
        public String doThat()
        {
            return "that";
        }
    }
}
