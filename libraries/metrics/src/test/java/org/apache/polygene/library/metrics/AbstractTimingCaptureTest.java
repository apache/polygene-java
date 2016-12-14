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
package org.apache.polygene.library.metrics;

import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.metrics.MetricValuesProvider;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

abstract class AbstractTimingCaptureTest extends AbstractPolygeneTest
{

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.layer().application().setName( "SomeApplication" );
        module.transients( Country1.class );
        module.transients( Country2.class ).withConcerns( TimingCaptureAllConcern.class );
        module.transients( Country3.class ).withConcerns( TimingCaptureConcern.class );
        metricsAssembler().assemble( module );
    }

    protected abstract Assemblers.Visible<? extends Assembler> metricsAssembler();

    protected abstract MetricValuesProvider metricValuesProvider();

    @Test
    public void givenNonInstrumentedCompositeExpectNoTimers()
    {
        Country underTest = transientBuilderFactory.newTransient( Country1.class );
        updateName( underTest, 10 );
        assertThat( metricValuesProvider().timerCount( "Layer 1.Module 1.AbstractTimingCaptureTest.Country.name" ), is( 0L ) );
        assertThat( metricValuesProvider().timerCount( "Layer 1.Module 1.AbstractTimingCaptureTest.Country.updateName" ), is( 0L ) );
    }

    @Test
    public void givenInstrumentedWithAllCompositeWhenCallingUpdateNameExpectTimers()
    {
        Country underTest = transientBuilderFactory.newTransient( Country2.class );
        updateName( underTest, 10 );
        assertThat( metricValuesProvider().timerCount( "Layer 1.Module 1.AbstractTimingCaptureTest.Country.name" ), is( 10L ) );
        assertThat( metricValuesProvider().timerCount( "Layer 1.Module 1.AbstractTimingCaptureTest.Country.updateName" ), is( 10L ) );
    }

    @Test
    public void givenOneMethodAnnotatedWhenCallingUpdateNameExpectTimerForThatMethodOnly()
    {
        Country underTest = transientBuilderFactory.newTransient( Country3.class );
        updateName( underTest, 10 );
        assertThat( metricValuesProvider().timerCount( "Layer 1.Module 1.AbstractTimingCaptureTest.Country.name" ), is( 0L ) );
        assertThat( metricValuesProvider().timerCount( "Country3.updateName" ), is( 10L ) );
    }

    private void updateName( Country underTest, int times )
    {
        for( int i = 0; i < times; i++ )
        {
            underTest.updateName( "Name" + i );
        }
    }

    // START SNIPPET: complex-capture
    public interface Country extends TransientComposite
    {
        @Optional
        Property<String> name();

        void updateName( String newName );
    }

    @Mixins( Country1Mixin.class )
    public interface Country1 extends Country
    {
    }

    public static abstract class Country1Mixin
            implements Country1
    {
        @Override
        public void updateName( String newName )
        {
            name().set( newName );
        }
    }

    @Mixins( Country2Mixin.class )
    public interface Country2 extends Country
    {
    }

    public static abstract class Country2Mixin
            implements Country2
    {
        @Override
        public void updateName( String newName )
        {
            name().set( newName );
        }
    }

    @Mixins( Country3Mixin.class )
    public interface Country3 extends Country
    {
        @TimingCapture( "Country3.updateName" )
        @Override
        void updateName(String newName);
    }

    public static abstract class Country3Mixin
            implements Country3
    {
        @Override
        public void updateName( String newName )
        {
            name().set( newName );
        }
    }
    // END SNIPPET: complex-capture
}
