/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.tutorials.cargo.step2;

import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.runtime.property.PropertyInstance;
import org.qi4j.test.mock.MockComposite;
import org.qi4j.test.mock.MockPlayerMixin;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Unit tests for SequencingConcern.
 *
 * @author Alin Dreghiciu
 */
public class SequencingConcernTest
{
    /**
     * Tests that when shipping service fails to make the booking generator is not called and booking failure code is
     * returned.
     */
    @Test
    @Ignore( "Expectations need to be figured out." )
    public void failingBooking()
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addTransients( ShippingServiceTestComposite.class );
            }
        };
        ShippingService shippingService = createMock( ShippingService.class );
        Cargo cargo = createMock( Cargo.class );
        Voyage voyage = createMock( Voyage.class );
        HasSequence sequence = createMock( HasSequence.class );
        expect( shippingService.makeBooking( cargo, voyage ) ).andReturn( -1000 );
        expect( voyage.bookedCargoSize() ).andReturn( new PropertyInstance<Double>( new GenericPropertyInfo( Voyage.class, "bookedCargoSize" ), 0.0, null ) )
            .atLeastOnce();
        expect( cargo.size() ).andReturn( new PropertyInstance<Double>( new GenericPropertyInfo( Cargo.class, "size" ), 0.0, null ) )
            .atLeastOnce();
        expect( sequence.sequence() ).andReturn( new PropertyInstance<Integer>( new GenericPropertyInfo( HasSequence.class, "sequence" ), 0, null ) )
            .atLeastOnce();
        replay( shippingService, cargo, voyage );
        ShippingServiceTestComposite underTest =
            assembler.transientBuilderFactory().newTransient( ShippingServiceTestComposite.class );
        underTest.useMock( shippingService ).forClass( ShippingService.class );
        assertThat( "Booking result", underTest.makeBooking( cargo, voyage ), is( equalTo( -1000 ) ) );
        verify( shippingService, cargo, voyage );
    }

    /**
     * Tests that when shipping service succeeds to make the booking generator gets called and generated value is
     * returned.
     */
    @Test
    @Ignore( "Expectations need to be figured out." )
    public void succesfulBooking()
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addTransients( ShippingServiceTestComposite.class );
            }
        };
        ShippingService shippingService = createMock( ShippingService.class );
        Cargo cargo = createMock( Cargo.class );
        Voyage voyage = createMock( Voyage.class );
        HasSequence generator = createMock( HasSequence.class );
        Property<Integer> sequence = createMock( Property.class );
        expect( shippingService.makeBooking( cargo, voyage ) ).andReturn( 100 );
        expect( generator.sequence() ).andReturn( sequence ).anyTimes();
        expect( sequence.get() ).andReturn( 1000 );
        replay( shippingService, cargo, voyage, generator, sequence );
        ShippingServiceTestComposite underTest =
            assembler.transientBuilderFactory().newTransient( ShippingServiceTestComposite.class );
        underTest.useMock( shippingService ).forClass( ShippingService.class );
        underTest.useMock( generator ).forClass( HasSequence.class );
        assertThat( "Booking result", underTest.makeBooking( cargo, voyage ), is( equalTo( 1000 ) ) );
        verify( shippingService, cargo, voyage, generator, sequence );
    }

    @Mixins( MockPlayerMixin.class )
    @Concerns( SequencingConcern.class )
    public static interface ShippingServiceTestComposite
        extends ShippingService, HasSequence, MockComposite
    {
    }
}
