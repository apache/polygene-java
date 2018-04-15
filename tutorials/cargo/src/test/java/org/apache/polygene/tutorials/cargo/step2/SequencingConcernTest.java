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
package org.apache.polygene.tutorials.cargo.step2;

import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.concern.Concerns;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.apache.polygene.test.mock.MockComposite;
import org.apache.polygene.test.mock.MockPlayerMixin;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for SequencingConcern.
 *
 */
public class SequencingConcernTest
{
    /**
     * Tests that when shipping service fails to make the booking generator is not called and booking failure code is
     * returned.
     */
    @Test
    @Disabled( "Expectations need to be figured out." )
    public void failingBooking()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler(
            module -> module.transients( ShippingServiceTestComposite.class )
        );
        ShippingService shippingService = createMock( ShippingService.class );
        Cargo cargo = createMock( Cargo.class );
        Voyage voyage = createMock( Voyage.class );
        HasSequence sequence = createMock( HasSequence.class );
        expect( shippingService.makeBooking( cargo, voyage ) ).andReturn( -1000 );
        expect( voyage.bookedCargoSize().get() ).andReturn( 0.0 )
            .atLeastOnce();
        expect( cargo.size().get() ).andReturn( 0.0 )
            .atLeastOnce();
        expect( sequence.sequence().get() ).andReturn( 0 )
            .atLeastOnce();
        replay( shippingService, cargo, voyage );
        ShippingServiceTestComposite underTest =
            assembler.module().newTransient( ShippingServiceTestComposite.class );
        underTest.useMock( shippingService ).forClass( ShippingService.class );
        assertThat( "Booking result", underTest.makeBooking( cargo, voyage ), is( equalTo( -1000 ) ) );
        verify( shippingService, cargo, voyage );
    }

    /**
     * Tests that when shipping service succeeds to make the booking generator gets called and generated value is
     * returned.
     */
    @Test
    @Disabled( "Expectations need to be figured out." )
    public void successfulBooking()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler(
            module -> module.transients( ShippingServiceTestComposite.class )
        );
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
            assembler.module().newTransient( ShippingServiceTestComposite.class );
        underTest.useMock( shippingService ).forClass( ShippingService.class );
        underTest.useMock( generator ).forClass( HasSequence.class );
        assertThat( "Booking result", underTest.makeBooking( cargo, voyage ), is( equalTo( 1000 ) ) );
        verify( shippingService, cargo, voyage, generator, sequence );
    }

    @Mixins( MockPlayerMixin.class )
    @Concerns( SequencingConcern.class )
    public interface ShippingServiceTestComposite
        extends ShippingService, HasSequence, MockComposite
    {
    }
}
