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

package org.apache.zest.runtime.sideeffects;

import org.junit.Test;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.composite.TransientBuilderFactory;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.sideeffect.SideEffectOf;
import org.apache.zest.api.sideeffect.SideEffects;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractPolygeneTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SampleTransientTest extends AbstractPolygeneTest
{

    @Structure
    TransientBuilderFactory transientBuilderFactory;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( SampleTransient.class );
    }

    @Test
    public void givenTransientWithSideEffectsWhenInvokingMethodShouldExecuteSideEffectOnlyOnce()
    {
        SampleTransient sample = transientBuilderFactory.newTransient( SampleTransient.class );
        sample.execute();
        assertThat( sample.count().get(), equalTo(1));
    }

    @SideEffects( SampleSideEffect.class )
    @Mixins( SampleTransientMixin.class )
    public static interface SampleTransient
    {
        void execute();

        @UseDefaults
        Property<Integer> count();
    }

    public abstract static class SampleTransientMixin
        implements SampleTransient
    {
        @Override
        public void execute()
        {
            System.out.println( "Invocation of Transient" );
        }
    }

    public abstract static class SampleSideEffect extends SideEffectOf<SampleTransient>
        implements SampleTransient
    {
        @Override
        public void execute()
        {
            System.out.println( "Invocation of SideEffect" );
            count().set( count().get() + 1 );
        }
    }
}
