/*
 * Copyright 2009 Niclas Hedhman.
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

package org.qi4j.sample.rental.web.assembly;

import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.sample.rental.domain.Address;
import org.qi4j.sample.rental.domain.Booking;
import org.qi4j.sample.rental.domain.Car;
import org.qi4j.sample.rental.domain.CarCategory;
import org.qi4j.sample.rental.domain.Customer;
import org.qi4j.sample.rental.domain.Period;
import org.qi4j.sample.rental.domain.RentalShop;
import org.qi4j.sample.rental.domain.dev.InitialData;

import static org.qi4j.api.common.Visibility.application;

public class RentalModule
    implements Assembler
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( Booking.class, Car.class, Customer.class, CarCategory.class ).visibleIn( application );
        module.values( Address.class, Period.class );
        module.entities( RentalShop.class ).visibleIn( application );

        if( module.layer().application().mode().equals( Application.Mode.development ) )
        {
            module.transients( InitialData.class ).visibleIn( application );
        }
    }
}
