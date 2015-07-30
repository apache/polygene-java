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

package org.apache.zest.sample.rental.web.assembly;

import org.apache.zest.api.structure.Application;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.sample.rental.domain.Address;
import org.apache.zest.sample.rental.domain.Booking;
import org.apache.zest.sample.rental.domain.Car;
import org.apache.zest.sample.rental.domain.CarCategory;
import org.apache.zest.sample.rental.domain.Customer;
import org.apache.zest.sample.rental.domain.Period;
import org.apache.zest.sample.rental.domain.RentalShop;
import org.apache.zest.sample.rental.domain.dev.InitialData;

import static org.apache.zest.api.common.Visibility.application;

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
