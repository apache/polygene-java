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
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class RuntimeMixinsTest
{
    @Test
    public void givenValueWithRuntimeMixinsWhenAssembledExpectCorrectComposition()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler(
            module -> module.values( SayWhat.class ).withMixins( SayThisMixin.class, SayThatMixin.class )
        );

        SayWhat value = singletonAssembler.valueBuilderFactory().newValue( SayWhat.class );
        assertThat( value.sayThis(), equalTo( "this" ) );
        assertThat( value.sayThat(), equalTo( "that" ) );
    }

    @Test
    public void givenValueWithRuntimeMixinOverrideWhenAssembledExpectCorrectComposition()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler(
            module -> module.values( SayWhere.class ).withMixins( SayHereMixin.class )
        );
        SayWhere value = singletonAssembler.valueBuilderFactory().newValue( SayWhere.class );
        assertThat( value.sayHere(), equalTo( "here" ) );
        assertThat( value.sayThere(), nullValue() );
    }

    @Test
    public void givenTransientWithRuntimeMixinsWhenAssembledExpectCorrectComposition()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler(
            module -> module.transients( SayWhat.class ).withMixins( SayThisMixin.class, SayThatMixin.class )
        );
        SayWhat value = singletonAssembler.transientBuilderFactory().newTransient( SayWhat.class );
        assertThat( value.sayThis(), equalTo( "this" ) );
        assertThat( value.sayThat(), equalTo( "that" ) );
    }

    @Test
    public void givenTransientWithRuntimeMixinOverrideWhenAssembledExpectCorrectComposition()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler(
            module -> module.transients( SayWhere.class ).withMixins( SayHereMixin.class )
        );
        SayWhere value = singletonAssembler.transientBuilderFactory().newTransient( SayWhere.class );
        assertThat( value.sayHere(), equalTo( "here" ) );
        assertThat( value.sayThere(), nullValue() );
    }

    @Test
    public void givenServiceWithRuntimeMixinsWhenAssembledExpectCorrectComposition()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler(
            module -> module.services( SayWhat.class ).withMixins( SayThisMixin.class, SayThatMixin.class )
        );
        SayWhat value = singletonAssembler.serviceFinder().findService( SayWhat.class ).get();
        assertThat( value.sayThis(), equalTo( "this" ) );
        assertThat( value.sayThat(), equalTo( "that" ) );
    }

    @Test
    public void givenServiceWithRuntimeMixinOverrideWhenAssembledExpectCorrectComposition()
        throws ActivationException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler(
            module -> module.services( SayWhere.class ).withMixins( SayHereMixin.class )
        );
        SayWhere value = singletonAssembler.serviceFinder().findService( SayWhere.class ).get();
        assertThat( value.sayHere(), equalTo( "here" ) );
        assertThat( value.sayThere(), nullValue() );
    }

    public interface SayWhat
    {
        String sayThis();

        String sayThat();
    }

    @Mixins( NoopMixin.class )
    public interface SayWhere
    {
        String sayHere();

        String sayThere();
    }

    protected abstract static class SayThisMixin
        implements SayWhat
    {
        @Override
        public String sayThis()
        {
            return "this";
        }
    }

    protected abstract static class SayThatMixin
        implements SayWhat
    {
        @Override
        public String sayThat()
        {
            return "that";
        }
    }

    protected abstract static class SayHereMixin
        implements SayWhere
    {
        @Override
        public String sayHere()
        {
            return "here";
        }
    }
}
