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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.TransientComposite;
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

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.layer().application().setName( "SomeApplication" );
        module.transients( Country1.class);
        module.transients( Country2.class ).withConcerns( TimingCaptureAllConcern.class );
//        module.transients( Person3.class ).withConcerns( TimingCaptureConcern.class );
        result = new ByteArrayOutputStream();
        reportOut = new PrintStream( result );
        new YammerMetricsAssembler( reportOut, 100, TimeUnit.MILLISECONDS ).assemble( module );
    }

    @Test
    public void givenNonInstrumentedCompositeWhenCallingUpdateNameExpectNoReport()
    {
        Country underTest = module.newTransient( Country1.class );
        String result = runTest( underTest );
        assertTrue( lastLine( result, 1 ).contains( "=====================" ) );
    }

    @Test
    public void givenInstrumentedCompositeWhenCallingUpdateNameExpectReport()
    {
        Country underTest = module.newTransient( Country2.class );
        String result = runTest( underTest );
        System.out.println(result);
        assertThat( lastLine( result, 34 ), equalTo( "org.qi4j.library.metrics.Country.SomeApplication:" ) );
        assertThat( lastLine( result, 16 ).trim(), equalTo( "updateName() [TimingCapture]:" ) );
        assertTrue( lastLine( result, 5 ).contains( "75% <=" ) );
        assertTrue( lastLine( result, 4 ).contains( "95% <=" ) );
        assertTrue( lastLine( result, 3 ).contains( "98% <=" ) );
        assertTrue( lastLine( result, 2 ).contains( "99% <=" ) );
        assertTrue( lastLine( result, 1 ).contains( "99.9% <=" ) );
    }

    private String lastLine( String text, int index )
    {
        String[] lines = text.split( "\n" );
        return lines[lines.length-index];
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
        @TimingCapture
        public void updateName( String newName )
        {
            name().set( newName );
        }
    }
}
