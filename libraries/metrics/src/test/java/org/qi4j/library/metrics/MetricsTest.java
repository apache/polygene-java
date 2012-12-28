/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.metrics;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.metrics.yammer.YammerMetricsAssembler;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MetricsTest extends AbstractQi4jTest
{
    private PrintStream reportOut;
    private ByteArrayOutputStream result;
    private YammerMetricsAssembler assembler;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.layer().application().setName( "SomeApplication" );
        module.transients( Country1.class );
        module.transients( Country2.class ).withConcerns( TimingCaptureAllConcern.class );
        module.transients( Country3.class ).withConcerns( TimingCaptureConcern.class );
        result = new ByteArrayOutputStream();
        reportOut = new PrintStream( result );
        assembler = new YammerMetricsAssembler( reportOut, 100, TimeUnit.MILLISECONDS );
        assembler.assemble( module );
    }

    @Override
    public void tearDown()
        throws Exception
    {
        if( assembler != null )  // Is null if assemble fails...
        {
            assembler.shutdown();
        }
        Field metrics = MetricsRegistry.class.getDeclaredField( "metrics" );
        metrics.setAccessible( true );
        Map m = (Map) metrics.get( Metrics.defaultRegistry() );
        m.clear();
        super.tearDown();
    }

    @Test
    public void givenNonInstrumentedCompositeWhenCallingUpdateNameExpectNoReport()
    {
        Country underTest = module.newTransient( Country1.class );
        String result = runTest( underTest );
        assertTrue( lastLine( result, 1 ).contains( "=====================" ) );
        System.out.println( "---END TEST---" );
    }

    @Test
    public void givenInstrumentedWithAllCompositeWhenCallingUpdateNameExpectReport()
    {
        Country underTest = module.newTransient( Country2.class );
        String result = runTest( underTest );
        assertThat( lastLine( result, 34 ), equalTo( "org.qi4j.library.metrics.Country.SomeApplication:" ) );
        assertThat( lastLine( result, 33 ).trim(), equalTo( "name() [TimingCapture]:" ) );
        assertThat( lastLine( result, 16 ).trim(), equalTo( "updateName() [TimingCapture]:" ) );
        assertTrue( lastLine( result, 5 ).contains( "75% <=" ) );
        assertTrue( lastLine( result, 4 ).contains( "95% <=" ) );
        assertTrue( lastLine( result, 3 ).contains( "98% <=" ) );
        assertTrue( lastLine( result, 2 ).contains( "99% <=" ) );
        assertTrue( lastLine( result, 1 ).contains( "99.9% <=" ) );
        System.out.println( "---END TEST---" );
    }

    @Test
    public void givenOneMethodAnnotatedWhenCallingUpdateNameExpectReportForThatMethodOnly()
    {
        Country underTest = module.newTransient( Country3.class );
        String result = runTest( underTest );
        System.out.println( result );
        assertThat( lastLine( result, 17 ), equalTo( "org.qi4j.library.metrics.Country.SomeApplication:" ) );
        assertThat( lastLine( result, 16 ).trim(), equalTo( "updateName() [TimingCapture]:" ) );
        assertTrue( lastLine( result, 5 ).contains( "75% <=" ) );
        assertTrue( lastLine( result, 4 ).contains( "95% <=" ) );
        assertTrue( lastLine( result, 3 ).contains( "98% <=" ) );
        assertTrue( lastLine( result, 2 ).contains( "99% <=" ) );
        assertTrue( lastLine( result, 1 ).contains( "99.9% <=" ) );
        System.out.println( "---END TEST---" );
    }

    private String lastLine( String text, int index )
    {
        String[] lines = text.split( "\n" );
        return lines[ lines.length - index ];
    }

    private String runTest( Country underTest )
    {

        for( int i = 0; i < 1000000; i++ )
        {
            underTest.updateName( "Name" + i );
        }
        reportOut.close();
        return result.toString();
    }

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
    @Concerns( TimingCaptureConcern.class )
    public interface Country3 extends Country
    {
    }

    public static abstract class Country3Mixin
        implements Country3
    {

        @Override
        @TimingCapture
        public void updateName( String newName )
        {
            name().set( newName );
        }
    }
}
