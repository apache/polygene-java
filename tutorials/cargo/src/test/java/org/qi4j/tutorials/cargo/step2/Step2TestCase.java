/*
 * Copyright 2007, 2008 Niclas Hedhman.
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

import org.junit.Test;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertEquals;

public class Step2TestCase
    extends AbstractQi4jTest
{
    private Voyage voyage;
    private ShippingService shippingService;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        TransientBuilder<VoyageComposite> voyageBuilder = module.newTransientBuilder( VoyageComposite.class );
        voyageBuilder.prototypeFor( Voyage.class ).bookedCargoSize().set( 0.0 );
        voyageBuilder.prototypeFor( Voyage.class ).capacity().set( 100.0 );
        voyage = voyageBuilder.newInstance();

        TransientBuilder<ShippingServiceComposite> shippingBuilder =
            module.newTransientBuilder( ShippingServiceComposite.class );
        shippingService = shippingBuilder.newInstance();
    }

    @Test
    public void testOrdinaryBooking()
    {
        Cargo cargo1 = newCargo( 40 );
        Cargo cargo2 = newCargo( 40 );
        Cargo cargo3 = newCargo( 20 );
        int code = shippingService.makeBooking( cargo1, voyage );
        assertEquals( 0, code );
        code = shippingService.makeBooking( cargo2, voyage );
        assertEquals( 1, code );
        code = shippingService.makeBooking( cargo3, voyage );
        assertEquals( 2, code );
    }

    @Test
    public void testOverbooking()
    {
        Cargo cargo1 = newCargo( 100 );
        Cargo cargo2 = newCargo( 9 );
        int code = shippingService.makeBooking( cargo1, voyage );
        assertEquals( 0, code );
        code = shippingService.makeBooking( cargo2, voyage );
        assertEquals( -1, code );
    }

    @Test
    public void testTooMuch()
    {
        Cargo cargo1 = newCargo( 40 );
        Cargo cargo2 = newCargo( 40 );
        Cargo cargo3 = newCargo( 31 );
        int code = shippingService.makeBooking( cargo1, voyage );
        assertEquals( 0, code );
        code = shippingService.makeBooking( cargo2, voyage );
        assertEquals( 1, code );
        code = shippingService.makeBooking( cargo3, voyage );
        assertEquals( -1, code );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( VoyageComposite.class, CargoComposite.class, ShippingServiceComposite.class );
    }

    private Cargo newCargo( double size )
    {
        TransientBuilder<CargoComposite> builder = module.newTransientBuilder( CargoComposite.class );
        builder.prototypeFor( Cargo.class ).size().set( size );
        return builder.newInstance();
    }
}
