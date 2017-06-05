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

package org.apache.polygene.sample.rental.web.assembly;

import org.apache.polygene.api.structure.Application;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.sample.rental.domain.Address;
import org.apache.polygene.sample.rental.domain.Booking;
import org.apache.polygene.sample.rental.domain.Car;
import org.apache.polygene.sample.rental.domain.CarCategory;
import org.apache.polygene.sample.rental.domain.Customer;
import org.apache.polygene.sample.rental.domain.Period;
import org.apache.polygene.sample.rental.domain.RentalShop;
import org.apache.polygene.sample.rental.domain.dev.InitialData;

import static org.apache.polygene.api.common.Visibility.application;

public class RentalModule
    implements Assembler
{
    public void assemble( ModuleAssembly module )
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
