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

package org.apache.polygene.sample.rental.domain.dev;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueBuilderFactory;
import org.apache.polygene.sample.rental.domain.Car;
import org.apache.polygene.sample.rental.domain.Customer;
import org.apache.polygene.sample.rental.domain.Period;
import org.apache.polygene.sample.rental.domain.RentalShop;
import org.apache.polygene.sample.rental.web.DataInitializer;

@Mixins( InitialData.Mixin.class )
public interface InitialData
    extends DataInitializer, TransientComposite
{
    abstract class Mixin
        implements DataInitializer
    {
        private static final Random random = new Random();

        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        private ArrayList<Customer> customers = new ArrayList<>();
        private ArrayList<Car> cars = new ArrayList<>();

        public void initialize()
            throws Exception
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                RentalShop shop = createShop( uow );
                createCustomers( shop );
                createCars( shop );
                createBookings( shop );
                uow.complete();
            }
            finally
            {
                uow.discard();
            }
        }

        private void createBookings( RentalShop shop )
        {
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
            shop.book( findRandomCustomer(), findRandomCar(), createRandomPeriod() );
        }

        private Period createRandomPeriod()
        {
            LocalDate start = LocalDate.now().plusDays( random.nextInt( 5));
            LocalDate end = start.plusDays( random.nextInt(30));

            ValueBuilder<Period> builder = vbf.newValueBuilder( Period.class );
            builder.prototype().startOfPeriod().set( start );
            builder.prototype().endOfPeriod().set( end );
            return builder.newInstance();
        }

        private Car findRandomCar()
        {
            int index = (int) Math.floor( cars.size() * Math.random() );
            return cars.get( index );
        }

        private Customer findRandomCustomer()
        {
            int index = (int) Math.floor( customers.size() * Math.random() );
            return customers.get( index );
        }

        private void createCars( RentalShop shop )
        {
            Car car;
            car = shop.createCar( "SUV", "Volvo XC90", "WHO 7878" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "SUV", "BMW X5", "WIT 23" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "SUV", "Volvo XC90", "WHO 7879" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "SUV", "Volvo XC90", "WHO 7880" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "SUV", "BMW X5", "WIT 24" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "SUV", "BMW X5", "WIT 25" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "SUV", "BMW X5", "WIT 26" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "SUV", "BMW X5", "WIT 27" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "Compact", "Mini Cooper S", "WMY 40" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "Compact", "Mini Cooper S", "WMY 41" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "Compact", "Mini Cooper S", "WMY 42" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "Compact", "Mini Cooper S", "WMY 43" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "Compact", "Mini Cooper S", "WMY 44" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "Compact", "Mini Cooper S", "WMY 45" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "Sedan", "BMW 318i", "WRY 900" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "Sedan", "BMW 318i", "WRY 901" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
            car = shop.createCar( "Sedan", "BMW 318i", "WRY 902" );
            shop.boughtCar( car, randomPastDate() );
            cars.add( car );
        }

        private LocalDate randomPastDate()
        {
            return LocalDate.now().minusDays( random.nextInt( 700 ) );
        }

        private RentalShop createShop( UnitOfWork uow )
            throws UnitOfWorkCompletionException
        {
            return uow.newEntity( RentalShop.class, new StringIdentity( "SHOP" ) );
        }

        private void createCustomers( RentalShop shop )
        {
            customers.add( shop.createCustomer( "Niclas Hedhman", "344-28-2, Vista Damai", "Jalan Tun Razak", "50400", "Kuala Lumpur", "Malaysia" ) );
            customers.add( shop.createCustomer( "Peter Neubauer", "Sovstaden 24", "", "21212", "Malm�", "Sweden" ) );
            customers.add( shop.createCustomer( "Rickard �berg", "GpsFree 123", "Bukit Antarabangsa", "68000", "Ampang", "Malaysia" ) );
            customers.add( shop.createCustomer( "Mickey Mouse", "Playhouse 1", "", "1234", "Disneyland", "USA" ) );
            customers.add( shop.createCustomer( "Michael Hunger", "Jederstrasse 1", "", "78787", "Dresden", "Germany" ) );
            customers.add( shop.createCustomer( "Emil Eifrem", "Freeze Street 12", "WhereWindBlows", "23456", "Malm�", "Sweden" ) );
        }
    }
}
