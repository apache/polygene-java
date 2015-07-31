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
package org.qi4j.tutorials.cargo.step1;

import junit.framework.TestCase;
import org.qi4j.tutorials.cargo.step1.internal.CargoImpl;
import org.qi4j.tutorials.cargo.step1.internal.ShippingServiceImpl;
import org.qi4j.tutorials.cargo.step1.internal.VoyageImpl;

public class Step1TestCase
    extends TestCase
{
    private ShippingService shippingService;
    private Voyage voyage;

    @Override
    protected void setUp()
        throws Exception
    {
        BookingPolicy policy = new OverbookingPolicy();
        shippingService = new ShippingServiceImpl( policy );
        voyage = newVoyage( "Singapore", "New York" );
    }

    public void testOrdinaryBooking()
    {
        Cargo cargo1 = newCargo( 40 );
        Cargo cargo2 = newCargo( 40 );
        Cargo cargo3 = newCargo( 20 );
        int code = shippingService.makeBooking( cargo1, voyage );
        assertEquals( 1, code );
        code = shippingService.makeBooking( cargo2, voyage );
        assertEquals( 2, code );
        code = shippingService.makeBooking( cargo3, voyage );
        assertEquals( 3, code );
    }

    public void testOverbooking()
    {
        Cargo cargo1 = newCargo( 100 );
        Cargo cargo2 = newCargo( 9 );
        int code = shippingService.makeBooking( cargo1, voyage );
        assertEquals( 1, code );
        code = shippingService.makeBooking( cargo2, voyage );
        assertEquals( 2, code );
    }

    public void testTooMuch()
    {
        Cargo cargo1 = newCargo( 40 );
        Cargo cargo2 = newCargo( 40 );
        Cargo cargo3 = newCargo( 31 );
        int code = shippingService.makeBooking( cargo1, voyage );
        assertEquals( 1, code );
        code = shippingService.makeBooking( cargo2, voyage );
        assertEquals( 2, code );
        code = shippingService.makeBooking( cargo3, voyage );
        assertEquals( -1, code );
    }

    private Cargo newCargo( double size )
    {
        return new CargoImpl( size );
    }

    private Voyage newVoyage( String sourceCity, String destinationCity )
    {
        return new VoyageImpl( 100 );  // Fake the finder to just return a new Voyage for the demonstration purpose.
    }
}
